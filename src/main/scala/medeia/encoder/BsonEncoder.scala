package medeia.encoder

import java.time.Instant
import java.util.{Date, UUID}

import cats.Contravariant
import cats.data.Chain
import org.mongodb.scala.bson._
import org.mongodb.scala.bson.collection.{immutable, mutable}

@FunctionalInterface
trait BsonEncoder[A] { self =>
  def encode(value: A): BsonValue

  def contramap[B](f: B => A): BsonEncoder[B] = b => self.encode(f(b))
}

trait BsonDocumentEncoder[A] extends BsonEncoder[A] {
  override def encode(a: A): BsonDocument
}

object BsonEncoder extends DefaultBsonEncoderInstances {
  def apply[A: BsonEncoder]: BsonEncoder[A] = implicitly

  def encode[A: BsonEncoder](value: A): BsonValue = BsonEncoder[A].encode(value)

  implicit val contravariantBsonEncoder: Contravariant[BsonEncoder] =
    new Contravariant[BsonEncoder] {
      override def contramap[A, B](fa: BsonEncoder[A])(f: B => A): BsonEncoder[B] = fa.contramap(f).encode
    }
}

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

  implicit val uuidEncoder: BsonEncoder[UUID] = stringEncoder.contramap(_.toString)

  implicit def listEncoder[A: BsonEncoder]: BsonEncoder[List[A]] = iterableEncoder[A].contramap(_.toIterable)

  implicit def setEncoder[A: BsonEncoder]: BsonEncoder[Set[A]] = iterableEncoder[A].contramap(_.toIterable)

  implicit def vectorEncoder[A: BsonEncoder]: BsonEncoder[Vector[A]] = iterableEncoder[A].contramap(_.toIterable)

  implicit def chainEncoder[A: BsonEncoder]: BsonEncoder[Chain[A]] = iterableEncoder[A].contramap(_.toList.toIterable)

  implicit def mapEncoder[K: BsonKeyEncoder, A: BsonEncoder]: BsonEncoder[Map[K, A]] = (value: Map[K, A]) => {
    BsonDocument(value.map { case (k, a) => (BsonKeyEncoder[K].encode(k), BsonEncoder[A].encode(a)) })
  }

  implicit def bsonValueEncoder[A <: BsonValue]: BsonEncoder[A] = value => value

  implicit val immutableDocumentEncoder: BsonEncoder[immutable.Document] = _.toBsonDocument

  implicit val mutableDocumentEncoder: BsonEncoder[mutable.Document] = _.toBsonDocument
}

trait BsonIterableEncoder {
  def iterableEncoder[A: BsonEncoder]: BsonEncoder[Iterable[A]] =
    xs => BsonArray.fromIterable(xs.map(BsonEncoder[A].encode))
}
