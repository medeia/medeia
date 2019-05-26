package medeia.encoder

import java.time.Instant
import java.util.Date

import cats.Contravariant
import org.mongodb.scala.bson._

trait BsonEncoder[-A] { self =>
  def encode(a: A): BsonValue

  def contramap[B](f: B => A): BsonEncoder[B] = b => self.encode(f(b))
}

trait BsonDocumentEncoder[-A] extends BsonEncoder[A] {
  override def encode(a: A): BsonDocument
}

object BsonEncoder extends DefaultBsonEncoderInstances {
  def apply[A: BsonEncoder]: BsonEncoder[A] = implicitly

  implicit val contravariantBsonEncoder: Contravariant[BsonEncoder] =
    new Contravariant[BsonEncoder] {
      override def contramap[A, B](fa: BsonEncoder[A])(
          f: B => A): BsonEncoder[B] = fa.contramap(f).encode
    }
}

trait DefaultBsonEncoderInstances extends BsonEncoderLowPriorityInstances {
  implicit val booleanEncoder: BsonEncoder[Boolean] = x => BsonBoolean(x)

  implicit val stringEncoder: BsonEncoder[String] = x => BsonString(x)

  implicit val intEncoder: BsonEncoder[Int] = x => BsonInt32(x)

  implicit val longEncoder: BsonEncoder[Long] = x => BsonInt64(x)

  implicit val doubleEncoder: BsonEncoder[Double] = x => BsonDouble(x)

  implicit val instantEncoder: BsonEncoder[Instant] = x =>
    BsonDateTime(x.toEpochMilli)

  implicit val dateEncoder: BsonEncoder[Date] = x => BsonDateTime(x)

  implicit val binaryEncoder: BsonEncoder[Array[Byte]] = x => BsonBinary(x)

  implicit val symbolEncoder: BsonEncoder[Symbol] = x => BsonSymbol(x)

  implicit def optionEncoder[A: BsonEncoder]: BsonEncoder[Option[A]] =
    x => x.map(BsonEncoder[A].encode).getOrElse(BsonNull())
}

trait BsonEncoderLowPriorityInstances {
  implicit def iterableEncoder[A: BsonEncoder]: BsonEncoder[Iterable[A]] =
    xs => BsonArray(xs.map(BsonEncoder[A].encode))
}
