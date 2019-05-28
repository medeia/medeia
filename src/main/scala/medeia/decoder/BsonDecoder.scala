package medeia.decoder

import java.time.Instant
import java.util.Date

import cats.data.EitherNec
import cats.instances.parallel._
import cats.syntax.either._
import cats.syntax.parallel._
import medeia.decoder.BsonDecoderError.TypeMismatch
import org.bson.BsonType
import org.mongodb.scala.bson.BsonValue

import scala.collection.generic.CanBuildFrom

trait BsonDecoder[+A] { self =>

  def decode(bson: BsonValue): EitherNec[BsonDecoderError, A]

  def map[B](f: A => B): BsonDecoder[B] = x => self.decode(x).map(f(_))
}

object BsonDecoder extends DefaultBsonDecoderInstances {
  def apply[A: BsonDecoder]: BsonDecoder[A] = implicitly

  def decode[A: BsonDecoder](
      bson: BsonValue): EitherNec[BsonDecoderError, A] = {
    BsonDecoder[A].decode(bson)
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

  implicit def optionDecoder[A: BsonDecoder]: BsonDecoder[Option[A]] =
    bson =>
      bson.getBsonType match {
        case BsonType.NULL => Right(None)
        case _             => BsonDecoder[A].decode(bson).map(Some(_))
    }
}

trait BsonDecoderLowPriorityInstances {
  implicit def iterableDecoder[A: BsonDecoder, C[_] <: Iterable[A]](
      implicit canBuildFrom: CanBuildFrom[Nothing, A, C[A]])
    : BsonDecoder[C[A]] =
    bson =>
      bson.getBsonType match {
        case BsonType.ARRAY =>
          val builder = canBuildFrom()
          var elems = Either.rightNec[BsonDecoderError, builder.type](builder)

          bson.asArray.getValues.forEach { b =>
            val decoded = BsonDecoder[A].decode(b)
            elems = (elems, decoded).parMapN((builder, dec) => builder += dec)
          }

          elems.map(_.result())
        case t => Either.leftNec(TypeMismatch(t, BsonType.ARRAY))
    }
}
