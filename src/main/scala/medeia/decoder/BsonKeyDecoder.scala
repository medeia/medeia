package medeia.decoder

import cats.data.EitherNec

trait BsonKeyDecoder[A] { self =>
  def decode(string: String): EitherNec[BsonDecoderError, A]

  def map[B](f: A => B): BsonKeyDecoder[B] = (string: String) => self.decode(string).map(f)
}
