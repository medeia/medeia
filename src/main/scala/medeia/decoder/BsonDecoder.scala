package medeia.decoder

import java.time.Instant
import java.util.Date

import cats.data.EitherNec
import cats.syntax.either._
import medeia.decoder.BsonDecoderError.TypeMismatch
import org.bson.BsonType
import org.mongodb.scala.bson.BsonValue

trait BsonDecoder[A] { self =>

  def decodeFromBson(a: BsonValue): EitherNec[BsonDecoderError, A]

  def map[B](f: A => B): BsonDecoder[B] = x => self.decodeFromBson(x).map(f(_))
}

object BsonDecoder {
  def apply[A: BsonDecoder]: BsonDecoder[A] = implicitly

  def decode[A: BsonDecoder](a: BsonValue): EitherNec[BsonDecoderError, A] = {
    BsonDecoder[A].decodeFromBson(a)
  }

}

trait DefaultBsonDecoderInstances extends BsonDecoderLowPriorityInstances {
  implicit val booleanDecoder: BsonDecoder[Boolean] = bson =>
    bson.getBsonType match {
      case BsonType.BOOLEAN => Right(bson.asBoolean().getValue)
      case t                => Either.leftNec(TypeMismatch(t, BsonType.BOOLEAN))
  }

  implicit val stringDecoder: BsonDecoder[String] = bson =>
    bson.getBsonType match {
      case BsonType.STRING => Right(bson.asString.getValue)
      case t               => Either.leftNec(TypeMismatch(t, BsonType.STRING))
  }

  implicit val intDecoder: BsonDecoder[Int] = bson =>
    bson.getBsonType match {
      case BsonType.INT32 => Right(bson.asInt32.getValue)
      case t              => Either.leftNec(TypeMismatch(t, BsonType.INT32))
  }

  implicit val longDecoder: BsonDecoder[Long] = bson =>
    bson.getBsonType match {
      case BsonType.INT64 => Right(bson.asInt64.getValue)
      case t              => Either.leftNec(TypeMismatch(t, BsonType.INT64))
  }

  implicit val doubleDecoder: BsonDecoder[Double] = bson =>
    bson.getBsonType match {
      case BsonType.DOUBLE => Right(bson.asDouble().getValue)
      case t               => Either.leftNec(TypeMismatch(t, BsonType.DOUBLE))
  }

  implicit val instantDecoder: BsonDecoder[Instant] = bson =>
    bson.getBsonType match {
      case BsonType.DATE_TIME =>
        Right(Instant.ofEpochMilli(bson.asDateTime().getValue))
      case t => Either.leftNec(TypeMismatch(t, BsonType.DATE_TIME))
  }

  implicit val dateDecoder: BsonDecoder[Date] = instantDecoder.map(Date.from)

  implicit val binaryDecoder: BsonDecoder[Array[Byte]] = bson =>
    bson.getBsonType match {
      case BsonType.BINARY => Right(bson.asBinary().getData)
      case t               => Either.leftNec(TypeMismatch(t, BsonType.BINARY))
  }

  implicit val symbolDecoder: BsonDecoder[Symbol] = bson =>
    bson.getBsonType match {
      case BsonType.SYMBOL => Right(Symbol(bson.asSymbol().getSymbol))
      case t               => Either.leftNec(TypeMismatch(t, BsonType.SYMBOL))
  }
}

trait BsonDecoderLowPriorityInstances {}
