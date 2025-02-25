package medeia.encoder

import java.net.URI
import java.time.Instant
import java.util.{Date, UUID}
import cats.Contravariant
import cats.data.{Chain, NonEmptyChain, NonEmptyList, NonEmptyMap, NonEmptySet}

import org.mongodb.scala.bson._
import org.mongodb.scala.bson.collection.{immutable, mutable}

import scala.collection.immutable.SortedSet
import java.util.Locale

@FunctionalInterface
trait BsonEncoder[A] { self =>
  def encode(value: A): BsonValue

  def contramap[B](f: B => A): BsonEncoder[B] = b => self.encode(f(b))
}

@FunctionalInterface
trait BsonDocumentEncoder[A] extends BsonEncoder[A] { self =>
  override def encode(a: A): BsonDocument

  override def contramap[B](f: B => A): BsonDocumentEncoder[B] = b => self.encode(f(b))
}

object BsonDocumentEncoder extends BsonDocumentEncoderVersionSpecific {
  def apply[A: BsonDocumentEncoder]: BsonDocumentEncoder[A] = implicitly


  implicit val contravariantBsonEncoder: Contravariant[BsonDocumentEncoder] =
    new Contravariant[BsonDocumentEncoder] {
      override def contramap[A, B](fa: BsonDocumentEncoder[A])(f: B => A): BsonDocumentEncoder[B] = fa.contramap(f).encode
    }
}

object BsonEncoder extends DefaultBsonEncoderInstances {
  def apply[A: BsonEncoder]: BsonEncoder[A] = implicitly

  def encode[A: BsonEncoder](value: A): BsonValue = BsonEncoder[A].encode(value)

  implicit val contravariantBsonEncoder: Contravariant[BsonEncoder] =
    new Contravariant[BsonEncoder] {
      override def contramap[A, B](fa: BsonEncoder[A])(f: B => A): BsonEncoder[B] = fa.contramap(f).encode
    }
}

@SuppressWarnings(Array("org.wartremover.warts.RedundantConversions"))
trait DefaultBsonEncoderInstances extends BsonIterableEncoder {
  implicit val booleanEncoder: BsonEncoder[Boolean] = x => BsonBoolean(x)

  implicit val stringEncoder: BsonEncoder[String] = x => BsonString(x)

  implicit val intEncoder: BsonEncoder[Int] = x => BsonInt32(x)

  implicit val longEncoder: BsonEncoder[Long] = x => BsonInt64(x)

  implicit val doubleEncoder: BsonEncoder[Double] = x => BsonDouble(x)

  implicit val instantEncoder: BsonEncoder[Instant] = x => BsonDateTime(x.toEpochMilli)

  implicit val dateEncoder: BsonEncoder[Date] = x => BsonDateTime(x)

  implicit val binaryEncoder: BsonEncoder[Array[Byte]] = x => BsonBinary(x)

  implicit val symbolEncoder: BsonEncoder[Symbol] = x => BsonSymbol(x)

  implicit def optionEncoder[A: BsonEncoder]: BsonEncoder[Option[A]] =
    x => x.map(BsonEncoder[A].encode).getOrElse(BsonNull())

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  implicit val uuidEncoder: BsonEncoder[UUID] = stringEncoder.contramap(_.toString)

  implicit val localeEncoder: BsonEncoder[Locale] = stringEncoder.contramap(_.toLanguageTag())

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  implicit val uriEncoder: BsonEncoder[URI] = stringEncoder.contramap(_.toString)

  implicit def listEncoder[A: BsonEncoder]: BsonEncoder[List[A]] = iterableEncoder[A].contramap(_.toList)

  implicit def setEncoder[A: BsonEncoder]: BsonEncoder[Set[A]] = iterableEncoder[A].contramap(_.toSet)

  implicit def sortedSetEncoder[A: BsonEncoder]: BsonEncoder[SortedSet[A]] = iterableEncoder[A].contramap(_.toSet)

  implicit def vectorEncoder[A: BsonEncoder]: BsonEncoder[Vector[A]] = iterableEncoder[A].contramap(_.toVector)

  implicit def chainEncoder[A: BsonEncoder]: BsonEncoder[Chain[A]] = iterableEncoder[A].contramap(_.toList)

  implicit def mapEncoder[K: BsonKeyEncoder, A: BsonEncoder]: BsonDocumentEncoder[Map[K, A]] =
    (value: Map[K, A]) => {
      BsonDocument(value.map { case (k, a) => (BsonKeyEncoder[K].encode(k), BsonEncoder[A].encode(a)) })
    }

  implicit def nonEmptyListEncoder[A: BsonEncoder]: BsonEncoder[NonEmptyList[A]] = listEncoder[A].contramap(_.toList)

  implicit def nonEmptyChainEncoder[A: BsonEncoder]: BsonEncoder[NonEmptyChain[A]] = chainEncoder[A].contramap(_.toChain)

  implicit def nonEmptySetEncoder[A: BsonEncoder]: BsonEncoder[NonEmptySet[A]] = sortedSetEncoder[A].contramap(_.toSortedSet)

  implicit def nonEmptyMapEncoder[K: BsonKeyEncoder, A: BsonEncoder]: BsonEncoder[NonEmptyMap[K, A]] = mapEncoder[K, A].contramap(_.toSortedMap)

  implicit def bsonValueEncoder[A <: BsonValue]: BsonEncoder[A] = value => value

  implicit val immutableDocumentEncoder: BsonEncoder[immutable.Document] = _.toBsonDocument

  implicit val mutableDocumentEncoder: BsonEncoder[mutable.Document] = _.toBsonDocument
}

trait BsonIterableEncoder {
  def iterableEncoder[A: BsonEncoder]: BsonEncoder[Iterable[A]] =
    xs => BsonArray.fromIterable(xs.map(BsonEncoder[A].encode))
}
