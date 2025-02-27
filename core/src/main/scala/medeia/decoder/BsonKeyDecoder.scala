package medeia.decoder

import java.util.UUID
import cats.Functor
import cats.syntax.either._
import medeia.decoder.BsonDecoderError.{FieldParseError, GenericDecoderError}
import java.util.Locale
import java.util.IllformedLocaleException

trait BsonKeyDecoder[A] { self =>
  def decode(key: String): Either[BsonDecoderError, A]

  def map[B](f: A => B): BsonKeyDecoder[B] = (key: String) => self.decode(key).map(f)

  def emap[B](f: A => Either[String, B]): BsonKeyDecoder[B] =
    (key: String) => self.decode(key).flatMap(f(_).leftMap(GenericDecoderError(_)))
}

object BsonKeyDecoder extends DefaultBsonKeyDecoderInstances {
  def apply[A: BsonKeyDecoder]: BsonKeyDecoder[A] = implicitly

  def decode[A: BsonKeyDecoder](key: String): Either[BsonDecoderError, A] = {
    BsonKeyDecoder[A].decode(key)
  }

  implicit val bsonKeyDecoderFunctor: Functor[BsonKeyDecoder] = new Functor[BsonKeyDecoder] {
    override def map[A, B](fa: BsonKeyDecoder[A])(f: A => B): BsonKeyDecoder[B] = fa.map(f)
  }
}

trait DefaultBsonKeyDecoderInstances {
  implicit val stringDecoder: BsonKeyDecoder[String] = key => Right(key)

  implicit val intDecoder: BsonKeyDecoder[Int] = key =>
    Either.catchOnly[NumberFormatException](key.toInt).leftMap(FieldParseError("Cannot parse int", _))

  implicit val longDecoder: BsonKeyDecoder[Long] = key =>
    Either.catchOnly[NumberFormatException](key.toLong).leftMap(FieldParseError("Cannot parse long", _))

  implicit val doubleDecoder: BsonKeyDecoder[Double] = key =>
    Either.catchOnly[NumberFormatException](key.toDouble).leftMap(FieldParseError("Cannot parse double", _))

  implicit val uuidDecoder: BsonKeyDecoder[UUID] = key =>
    Either.catchOnly[IllegalArgumentException](UUID.fromString(key)).leftMap(FieldParseError("Cannot parse UUID", _))

  implicit val localeDecoder: BsonKeyDecoder[Locale] = key =>
    Either
      .catchOnly[IllformedLocaleException](new Locale.Builder().setLanguageTag(key).build())
      .leftMap(FieldParseError("Cannot parse locale", _))
      
}
