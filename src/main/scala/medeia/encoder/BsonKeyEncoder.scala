package medeia.encoder

trait BsonKeyEncoder[A] { self =>
  def encode(value: A): String

  def contramap[B](f: B => A): BsonKeyEncoder[B] = (b: B) => self.encode(f(b))
}

object BsonKeyEncoder {
  def apply[A: BsonKeyEncoder]: BsonKeyEncoder[A] = implicitly
}