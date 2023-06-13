package medeia.encoder

import java.util.UUID

trait BsonKeyEncoder[A] { self =>
  def encode(value: A): String

  def contramap[B](f: B => A): BsonKeyEncoder[B] = (b: B) => self.encode(f(b))
}

object BsonKeyEncoder extends DefaultBsonKeyEncoderInstances {
  def apply[A: BsonKeyEncoder]: BsonKeyEncoder[A] = implicitly

  def encode[A: BsonKeyEncoder](a: A): String = BsonKeyEncoder[A].encode(a)
}

trait DefaultBsonKeyEncoderInstances {
  implicit val stringEncoder: BsonKeyEncoder[String] = value => value

  implicit val intEncoder: BsonKeyEncoder[Int] = value => value.toString

  implicit val longEncoder: BsonKeyEncoder[Long] = value => value.toString

  implicit val doubleEncoder: BsonKeyEncoder[Double] = value => value.toString

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  implicit val uuidEncoder: BsonKeyEncoder[UUID] = value => value.toString
}
