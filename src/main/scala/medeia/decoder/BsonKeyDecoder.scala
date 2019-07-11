package medeia.decoder

import java.util.UUID

import cats.Functor
import cats.syntax.either._
import cats.data.EitherNec
import medeia.decoder.BsonDecoderError.FieldParseError

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
  implicit val stringDecoder: BsonKeyDecoder[String] = string => Right(string)

  implicit val intDecoder: BsonKeyDecoder[Int] = string =>
    Either.catchOnly[NumberFormatException](string.toInt).leftMap(FieldParseError(_)).toEitherNec

  implicit val longDecoder: BsonKeyDecoder[Long] = string =>
    Either.catchOnly[NumberFormatException](string.toLong).leftMap(FieldParseError(_)).toEitherNec

  implicit val doubleDecoder: BsonKeyDecoder[Double] = string =>
    Either.catchOnly[NumberFormatException](string.toDouble).leftMap(FieldParseError(_)).toEitherNec

  implicit val uuidDecoder: BsonKeyDecoder[UUID] = string =>
    Either.catchOnly[IllegalArgumentException](UUID.fromString(string)).leftMap(FieldParseError(_)).toEitherNec
}
