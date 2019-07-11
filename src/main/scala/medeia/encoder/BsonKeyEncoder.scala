package medeia.encoder

trait BsonKeyEncoder[A] { self =>
  def encode(value: A): String

  def contramap[B](f: B => A): BsonKeyEncoder[B] = (b: B) => self.encode(f(b))
}

object BsonKeyEncoder extends DefaultBsonKeyEncoderInstances {
  def apply[A: BsonKeyEncoder]: BsonKeyEncoder[A] = implicitly

  def encode[A: BsonKeyEncoder](a: A): String = BsonKeyEncoder[A].encode(a)
}

trait DefaultBsonKeyEncoderInstances {
  implicit val stringEncoder: BsonKeyEncoder[String] = (value: String) => value
}
