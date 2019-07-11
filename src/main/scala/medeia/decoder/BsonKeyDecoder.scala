package medeia.decoder

import cats.Functor
import cats.data.EitherNec

trait BsonKeyDecoder[A] { self =>
  def decode(string: String): EitherNec[BsonDecoderError, A]

  def map[B](f: A => B): BsonKeyDecoder[B] = (string: String) => self.decode(string).map(f)
}

object BsonKeyDecoder extends DefaultBsonKeyDecoderInstances {
  def apply[A: BsonKeyDecoder]: BsonKeyDecoder[A] = implicitly

  def decode[A: BsonKeyDecoder](string: String): EitherNec[BsonDecoderError, A] = {
    BsonKeyDecoder[A].decode(string)
  }

  implicit val bsonDecoderFunctor: Functor[BsonDecoder] = new Functor[BsonDecoder] {
    override def map[A, B](fa: BsonDecoder[A])(f: A => B): BsonDecoder[B] = fa.map(f)
  }
}

trait DefaultBsonKeyDecoderInstances {
  implicit val stringDecoder: BsonKeyDecoder[String] = (string: String) => Right(string)
}
