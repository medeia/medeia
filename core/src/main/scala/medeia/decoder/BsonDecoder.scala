package medeia.decoder

import java.time.Instant
import java.util.{Date, UUID}

import cats.{Functor, Order}
import cats.data.{Chain, EitherNec, NonEmptyChain, NonEmptyList, NonEmptySet}
import cats.syntax.either._
import cats.syntax.parallel._
import medeia.decoder.BsonDecoderError.{FieldParseError, TypeMismatch}
import medeia.generic.GenericDecoder
import medeia.generic.auto.AutoDerivationUnlocked
import medeia.generic.util.VersionSpecific.Lazy
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

import scala.collection.compat._
import scala.collection.immutable.SortedSet
import scala.jdk.CollectionConverters._

@FunctionalInterface
trait BsonDecoder[A] { self =>

  def decode(bson: BsonValue): EitherNec[BsonDecoderError, A]
  def defaultValue: Option[A] = None

  def map[B](f: A => B): BsonDecoder[B] = x => self.decode(x).map(f(_))
}

object BsonDecoder extends DefaultBsonDecoderInstances {
  def apply[A: BsonDecoder]: BsonDecoder[A] = implicitly

  def derive[A](implicit genericDecoder: Lazy[GenericDecoder[A]]): BsonDecoder[A] = genericDecoder.value

  def decode[A: BsonDecoder](bson: BsonValue): EitherNec[BsonDecoderError, A] = {
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
      case BsonType.DATE_TIME =>
        Right(Instant.ofEpochMilli(bson.asDateTime().getValue))
      case t => Either.leftNec(TypeMismatch(t, BsonType.DATE_TIME))
  }

  implicit val dateDecoder: BsonDecoder[Date] = instantDecoder.map(Date.from)

  implicit val binaryDecoder: BsonDecoder[Array[Byte]] = withType(BsonType.BINARY)(_.asBinary.getData)

  implicit val symbolDecoder: BsonDecoder[Symbol] = bson =>
    bson.getBsonType match {
      case BsonType.SYMBOL => Right(Symbol(bson.asSymbol().getSymbol))
      case t               => Either.leftNec(TypeMismatch(t, BsonType.SYMBOL))
  }

  implicit def optionDecoder[A: BsonDecoder]: BsonDecoder[Option[A]] =
    new BsonDecoder[Option[A]] {
      override def decode(bson: BsonValue): EitherNec[BsonDecoderError, Option[A]] = bson.getBsonType match {
        case BsonType.NULL => Right(None)
        case _             => BsonDecoder[A].decode(bson).map(Some(_))
      }

      override def defaultValue: Option[Option[A]] = Some(None)
    }

  implicit val uuidDecoder: BsonDecoder[UUID] = bson =>
    stringDecoder.decode(bson).flatMap { string =>
      Either.catchOnly[IllegalArgumentException](UUID.fromString(string)).leftMap(FieldParseError("Cannot parse UUID", _)).toEitherNec
  }

  implicit def listDecoder[A: BsonDecoder]: BsonDecoder[List[A]] = iterableDecoder

  implicit def setDecoder[A: BsonDecoder]: BsonDecoder[Set[A]] = iterableDecoder

  implicit def sortedSetDecoder[A: BsonDecoder: Ordering]: BsonDecoder[SortedSet[A]] = iterableDecoder

  implicit def vectorDecoder[A: BsonDecoder]: BsonDecoder[Vector[A]] = iterableDecoder

  implicit def chainDecoder[A: BsonDecoder]: BsonDecoder[Chain[A]] = listDecoder[A].map(Chain.fromSeq)

  implicit def mapDecoder[K: BsonKeyDecoder, A: BsonDecoder]: BsonDecoder[Map[K, A]] = bson => {
    val document = bsonDocumentDecoder.decode(bson)
    document.flatMap { doc: BsonDocument =>
      doc.asScala.toList
        .parTraverse {
          case (k, v) =>
            val key = BsonKeyDecoder[K].decode(k)
            val value = BsonDecoder[A].decode(v)
            (key, value).parMapN((k, v) => k -> v)
        }
        .map(_.toMap)
    }
  }

  implicit def nonEmptyListDecoder[A: BsonDecoder]: BsonDecoder[NonEmptyList[A]] =
    bson =>
      listDecoder[A]
        .decode(bson)
        .flatMap(list => NonEmptyList.fromList(list).toRight(FieldParseError("NonEmptyList may not be empty")).toEitherNec)

  implicit def nonEmptyChainDecoder[A: BsonDecoder]: BsonDecoder[NonEmptyChain[A]] =
    bson =>
      chainDecoder[A]
        .decode(bson)
        .flatMap(chain => NonEmptyChain.fromChain(chain).toRight(FieldParseError("NonEmptyChain may not be empty")).toEitherNec)

  implicit def nonEmptySetDecoder[A: BsonDecoder: Order]: BsonDecoder[NonEmptySet[A]] = {
    import Order.catsKernelOrderingForOrder
    bson =>
      sortedSetDecoder[A]
        .decode(bson)
        .flatMap(sortedSet => NonEmptySet.fromSet(sortedSet).toRight(FieldParseError("NonEmptySet may not be empty")).toEitherNec)
  }

  implicit val bsonValueDecoder: BsonDecoder[BsonValue] = Either.rightNec(_)

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

  private[this] def withType[A](expectedType: BsonType)(f: BsonValue => A)(bson: BsonValue): EitherNec[BsonDecoderError, A] = bson.getBsonType match {
    case `expectedType` => Right(f(bson))
    case _              => Either.leftNec(TypeMismatch(bson.getBsonType, expectedType))

  }
}

trait BsonIterableDecoder extends LowestPrioDecoderAutoDerivation {
  def iterableDecoder[A: BsonDecoder, C[_] <: Iterable[A]](implicit factory: Factory[A, C[A]]): BsonDecoder[C[A]] =
    bson =>
      bson.getBsonType match {
        case BsonType.ARRAY =>
          val builder = factory.newBuilder
          @SuppressWarnings(Array("org.wartremover.warts.Var"))
          var elems = Either.rightNec[BsonDecoderError, builder.type](builder)

          bson.asArray.getValues.forEach { b =>
            val decoded = BsonDecoder[A].decode(b)
            elems = (elems, decoded).parMapN((builder, dec) => builder += dec)
          }

          elems.map(_.result())
        case t => Either.leftNec(TypeMismatch(t, BsonType.ARRAY))
    }
}

trait LowestPrioDecoderAutoDerivation {
  final implicit def autoDerivedBsonEncoder[A: AutoDerivationUnlocked](
      implicit encoder: Lazy[GenericDecoder[A]]
  ): BsonDecoder[A] =
    BsonDecoder.derive[A](encoder)
}
