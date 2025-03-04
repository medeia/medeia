package medeia.decoder

import cats.data._
import cats.syntax.all._
import cats.{Functor, Order}
import medeia.decoder.BsonDecoderError.{FieldParseError, GenericDecoderError, TypeMismatch}
import medeia.decoder.StackFrame.{Case, Index, MapKey}

import org.bson.{BsonDbPointer, BsonDocument, BsonInt32, BsonType}
import org.mongodb.scala.bson.collection.{immutable, mutable}
import org.mongodb.scala.bson.{
  BsonArray,
  BsonBinary,
  BsonBoolean,
  BsonDateTime,
  BsonDecimal128,
  BsonDouble,
  BsonInt64,
  BsonJavaScript,
  BsonJavaScriptWithScope,
  BsonObjectId,
  BsonRegularExpression,
  BsonString,
  BsonSymbol,
  BsonTimestamp,
  BsonValue
}

import java.time.Instant
import java.util.{Date, UUID}
import scala.collection.immutable.{SortedMap, SortedSet}
import scala.collection.Factory
import scala.jdk.CollectionConverters._
import java.net.URI
import java.util.Locale
import java.util.IllformedLocaleException

@FunctionalInterface
trait BsonDecoder[A] { self =>

  def decode(bson: BsonValue): Either[BsonDecoderError, A]
  def defaultValue: Option[A] = None

  def map[B](f: A => B): BsonDecoder[B] = x => self.decode(x).map(f(_))

  def emap[B](f: A => Either[String, B]): BsonDecoder[B] =
    x => self.decode(x).flatMap(f(_).leftMap(GenericDecoderError(_)))
}

object BsonDecoder extends DefaultBsonDecoderInstances with BsonDecoderVersionSpecific {
  def apply[A: BsonDecoder]: BsonDecoder[A] = implicitly

  def decode[A: BsonDecoder](bson: BsonValue): Either[BsonDecoderError, A] = {
    BsonDecoder[A].decode(bson)
  }

  implicit val bsonDecoderFunctor: Functor[BsonDecoder] = new Functor[BsonDecoder] {
    override def map[A, B](fa: BsonDecoder[A])(f: A => B): BsonDecoder[B] = fa.map(f)
  }
}

trait DefaultBsonDecoderInstances extends BsonIterableDecoder {
  implicit val booleanDecoder: BsonDecoder[Boolean] = withType(BsonType.BOOLEAN)(_.asBoolean.getValue)

  implicit val stringDecoder: BsonDecoder[String] = withType(BsonType.STRING)(_.asString.getValue)

  implicit val intDecoder: BsonDecoder[Int] = withType(BsonType.INT32)(_.asInt32.getValue)

  implicit val longDecoder: BsonDecoder[Long] = withType(BsonType.INT64)(_.asInt64.getValue)

  implicit val doubleDecoder: BsonDecoder[Double] = withType(BsonType.DOUBLE)(_.asDouble.getValue)

  implicit val instantDecoder: BsonDecoder[Instant] = bson =>
    bson.getBsonType match {
      case BsonType.DATE_TIME => Right(Instant.ofEpochMilli(bson.asDateTime().getValue))
      case t                  => Left(TypeMismatch(t, BsonType.DATE_TIME))
    }

  implicit val dateDecoder: BsonDecoder[Date] = instantDecoder.map(Date.from)

  implicit val binaryDecoder: BsonDecoder[Array[Byte]] = withType(BsonType.BINARY)(_.asBinary.getData)

  implicit val symbolDecoder: BsonDecoder[Symbol] = bson =>
    bson.getBsonType match {
      case BsonType.SYMBOL => Right(Symbol(bson.asSymbol().getSymbol))
      case t               => Left(TypeMismatch(t, BsonType.SYMBOL))
    }

  implicit def optionDecoder[A: BsonDecoder]: BsonDecoder[Option[A]] =
    new BsonDecoder[Option[A]] {
      override def decode(bson: BsonValue): Either[BsonDecoderError, Option[A]] =
        bson.getBsonType match {
          case BsonType.NULL => Right(None)
          case _             => BsonDecoder[A].decode(bson).map(Some(_)).leftMap(_.push(Case("Some")))
        }

      override def defaultValue: Option[Option[A]] = Some(None)
    }

  implicit val uuidDecoder: BsonDecoder[UUID] = bson =>
    stringDecoder.decode(bson).flatMap { string =>
      Either.catchOnly[IllegalArgumentException](UUID.fromString(string)).leftMap(FieldParseError("Cannot parse UUID", _))
    }

  implicit val localeDecoder: BsonDecoder[Locale] = bson =>
    stringDecoder.decode(bson).flatMap { string =>
      Either
        .catchOnly[IllformedLocaleException](new Locale.Builder().setLanguageTag(string).build())
        .leftMap(FieldParseError("Cannot parse locale", _))

    }

  implicit val uriDecoder: BsonDecoder[URI] = bson =>
    stringDecoder.decode(bson).flatMap { string =>
      Either.catchOnly[IllegalArgumentException](URI.create(string)).leftMap(FieldParseError("Cannot parse URI", _))
    }

  implicit def listDecoder[A: BsonDecoder]: BsonDecoder[List[A]] = iterableDecoder

  implicit def setDecoder[A: BsonDecoder]: BsonDecoder[Set[A]] = iterableDecoder

  implicit def sortedSetDecoder[A: BsonDecoder: Ordering]: BsonDecoder[SortedSet[A]] = iterableDecoder

  implicit def vectorDecoder[A: BsonDecoder]: BsonDecoder[Vector[A]] = iterableDecoder

  implicit def chainDecoder[A: BsonDecoder]: BsonDecoder[Chain[A]] = listDecoder[A].map(Chain.fromSeq)

  implicit def mapDecoder[K: BsonKeyDecoder, A: BsonDecoder]: BsonDecoder[Map[K, A]] =
    bson => {
      val document = bsonDocumentDecoder.decode(bson)
      document.flatMap { (doc: BsonDocument) =>
        doc.asScala.toList
          .traverse { case (k, v) =>
            (for {
              key <- BsonKeyDecoder[K].decode(k)
              value <- BsonDecoder[A].decode(v)
            } yield key -> value).leftMap(_.push(MapKey(k)))
          }
          .map(_.toMap)
      }
    }

  implicit def nonEmptyListDecoder[A: BsonDecoder]: BsonDecoder[NonEmptyList[A]] =
    bson =>
      listDecoder[A]
        .decode(bson)
        .flatMap(list => NonEmptyList.fromList(list).toRight(FieldParseError("NonEmptyList may not be empty")))

  implicit def nonEmptyChainDecoder[A: BsonDecoder]: BsonDecoder[NonEmptyChain[A]] =
    bson =>
      chainDecoder[A]
        .decode(bson)
        .flatMap(chain => NonEmptyChain.fromChain(chain).toRight(FieldParseError("NonEmptyChain may not be empty")))

  implicit def nonEmptySetDecoder[A: BsonDecoder: Order]: BsonDecoder[NonEmptySet[A]] = {
    import Order.catsKernelOrderingForOrder
    bson =>
      sortedSetDecoder[A]
        .decode(bson)
        .flatMap(sortedSet => NonEmptySet.fromSet(sortedSet).toRight(FieldParseError("NonEmptySet may not be empty")))
  }

  implicit def nonEmptyMapDecoder[K: BsonKeyDecoder: Ordering, A: BsonDecoder]: BsonDecoder[NonEmptyMap[K, A]] =
    bson =>
      mapDecoder[K, A]
        .decode(bson)
        .flatMap(map => NonEmptyMap.fromMap(SortedMap.from(map)).toRight(FieldParseError("NonEmptyMap may not be empty")))

  implicit val bsonValueDecoder: BsonDecoder[BsonValue] = Right[BsonDecoderError, BsonValue](_)

  implicit val bsonArrayDecoder: BsonDecoder[BsonArray] = withType(BsonType.ARRAY)(_.asArray)

  implicit val bsonBinaryDecoder: BsonDecoder[BsonBinary] = withType(BsonType.BINARY)(_.asBinary)

  implicit val bsonBooleanDecoder: BsonDecoder[BsonBoolean] = withType(BsonType.BOOLEAN)(_.asBoolean)

  implicit val bsonDateTimeDecoder: BsonDecoder[BsonDateTime] = withType(BsonType.DATE_TIME)(_.asDateTime)

  implicit val bsonDbPointerDecoder: BsonDecoder[BsonDbPointer] = withType(BsonType.DB_POINTER)(_.asDBPointer)

  implicit val bsonDecimal128Decoder: BsonDecoder[BsonDecimal128] = withType(BsonType.DECIMAL128)(_.asDecimal128)

  implicit val bsonDocumentDecoder: BsonDecoder[BsonDocument] = withType(BsonType.DOCUMENT)(_.asDocument)

  implicit val bsonDoubleDecoder: BsonDecoder[BsonDouble] = withType(BsonType.DOUBLE)(_.asDouble)

  implicit val bsonInt32Decoder: BsonDecoder[BsonInt32] = withType(BsonType.INT32)(_.asInt32)

  implicit val bsonInt64Decoder: BsonDecoder[BsonInt64] = withType(BsonType.INT64)(_.asInt64)

  implicit val bsonJavaScriptDecoder: BsonDecoder[BsonJavaScript] = withType(BsonType.JAVASCRIPT)(_.asJavaScript)

  implicit val bsonJavaScriptWithScopeDecoder: BsonDecoder[BsonJavaScriptWithScope] =
    withType(BsonType.JAVASCRIPT_WITH_SCOPE)(_.asJavaScriptWithScope)

  implicit val bsonObjectIdDecoder: BsonDecoder[BsonObjectId] = withType(BsonType.OBJECT_ID)(_.asObjectId)

  implicit val bsonRegularExpressionDecoder: BsonDecoder[BsonRegularExpression] = withType(BsonType.REGULAR_EXPRESSION)(_.asRegularExpression)

  implicit val bsonStringDecoder: BsonDecoder[BsonString] = withType(BsonType.STRING)(_.asString)

  implicit val bsonSymbolDecoder: BsonDecoder[BsonSymbol] = withType(BsonType.SYMBOL)(_.asSymbol)

  implicit val bsonTimestampDecoder: BsonDecoder[BsonTimestamp] = withType(BsonType.TIMESTAMP)(_.asTimestamp)

  implicit val immutableDocumentDecoder: BsonDecoder[immutable.Document] = withType(BsonType.DOCUMENT)(b => immutable.Document(b.asDocument))

  implicit val mutableDocumentDecoder: BsonDecoder[mutable.Document] = withType(BsonType.DOCUMENT)(b => mutable.Document(b.asDocument))

  private[this] def withType[A](expectedType: BsonType)(f: BsonValue => A)(bson: BsonValue): Either[BsonDecoderError, A] =
    bson.getBsonType match {
      case `expectedType` => Right(f(bson))
      case _              => Left(TypeMismatch(bson.getBsonType, expectedType))

    }
}

trait BsonIterableDecoder {
  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  def iterableDecoder[A: BsonDecoder, C[_] <: Iterable[_]](implicit factory: Factory[A, C[A]]): BsonDecoder[C[A]] =
    bson =>
      bson.getBsonType match {
        case BsonType.ARRAY =>
          type BuilderType = scala.collection.mutable.Builder[A, C[A]]
          val builder: BuilderType = factory.newBuilder
          @SuppressWarnings(Array("org.wartremover.warts.Var"))
          var elems: Either[BsonDecoderError, BuilderType] = Right(builder)
          @SuppressWarnings(Array("org.wartremover.warts.Var"))
          var i = 0

          bson.asArray.getValues.forEach { b =>
            val decoded = BsonDecoder[A].decode(b).leftMap(_.push(Index(i)))
            elems = elems.flatMap(builder => decoded.map(dec => builder += dec))
            i += 1
          }

          elems.map(_.result())
        case t => Left(TypeMismatch(t, BsonType.ARRAY))
      }
}
