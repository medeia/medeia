package medeia.decoder

import java.util.UUID
import cats.Functor
import cats.syntax.either._
import cats.data.EitherNec
import medeia.decoder.BsonDecoderError.{FieldParseError, GenericDecoderError}

trait BsonKeyDecoder[A] { self =>
  def decode(key: String): EitherNec[BsonDecoderError, A]

  def map[B](f: A => B): BsonKeyDecoder[B] = (key: String) => self.decode(key).map(f)

  def emap[B](f: A => Either[String, B]): BsonKeyDecoder[B] =
    (key: String) => self.decode(key).flatMap(f(_).leftMap(GenericDecoderError(_)).toEitherNec)
}

object BsonKeyDecoder extends DefaultBsonKeyDecoderInstances {
  def apply[A: BsonKeyDecoder]: BsonKeyDecoder[A] = implicitly

  def decode[A: BsonKeyDecoder](key: String): EitherNec[BsonDecoderError, A] = {
    BsonKeyDecoder[A].decode(key)
  }

  implicit val bsonKeyDecoderFunctor: Functor[BsonKeyDecoder] = new Functor[BsonKeyDecoder] {
    override def map[A, B](fa: BsonKeyDecoder[A])(f: A => B): BsonKeyDecoder[B] = fa.map(f)
  }
}

trait DefaultBsonKeyDecoderInstances {
  implicit val stringDecoder: BsonKeyDecoder[String] = key => Right(key)

  implicit val intDecoder: BsonKeyDecoder[Int] = key =>
    Either.catchOnly[NumberFormatException](key.toInt).leftMap(FieldParseError("Cannot parse int", _)).toEitherNec

  implicit val longDecoder: BsonKeyDecoder[Long] = key =>
    Either.catchOnly[NumberFormatException](key.toLong).leftMap(FieldParseError("Cannot parse long", _)).toEitherNec

  implicit val doubleDecoder: BsonKeyDecoder[Double] = key =>
    Either.catchOnly[NumberFormatException](key.toDouble).leftMap(FieldParseError("Cannot parse double", _)).toEitherNec

  implicit val uuidDecoder: BsonKeyDecoder[UUID] = key =>
    Either.catchOnly[IllegalArgumentException](UUID.fromString(key)).leftMap(FieldParseError("Cannot parse UUID", _)).toEitherNec
}
