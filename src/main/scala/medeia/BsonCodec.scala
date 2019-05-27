package medeia

import cats.arrow.Profunctor
import cats.data.EitherNec
import medeia.decoder.{BsonDecoder, BsonDecoderError}
import medeia.encoder.BsonEncoder
import org.mongodb.scala.bson.BsonValue

trait BsonCodec[A] extends BsonCodecType[A, A]

object BsonCodec {
  def apply[A: BsonCodec]: BsonCodec[A] = implicitly

  implicit def fromBsonCodecType[A](
      implicit bsonCodecType: BsonCodecType[A, A]): BsonCodec[A] = {
    new BsonCodec[A] {
      override def decode(bson: BsonValue): EitherNec[BsonDecoderError, A] =
        bsonCodecType.decode(bson)

      override def encode(a: A): BsonValue = bsonCodecType.encode(a)
    }
  }
}

trait BsonCodecType[-A, +B] extends BsonEncoder[A] with BsonDecoder[B]

object BsonCodecType {
  implicit def fromEncoderAndDecoder[A, B](
      implicit encoder: BsonEncoder[A],
      decoder: BsonDecoder[B]): BsonCodecType[A, B] = new BsonCodecType[A, B] {
    override def encode(a: A): BsonValue = encoder.encode(a)

    override def decode(bson: BsonValue): EitherNec[BsonDecoderError, B] =
      decoder.decode(bson)
  }

  implicit val bsonCodecProfunctor: Profunctor[BsonCodecType] =
    new Profunctor[BsonCodecType] {
      override def dimap[A, B, C, D](fab: BsonCodecType[A, B])(f: C => A)(
          g: B => D): BsonCodecType[C, D] = new BsonCodecType[C, D] {
        override def encode(c: C): BsonValue = fab.encode(f(c))

        override def decode(bson: BsonValue): EitherNec[BsonDecoderError, D] =
          fab.decode(bson).map(g)
      }
    }
}
