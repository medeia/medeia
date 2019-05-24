package medeia

import cats.Invariant
import cats.data.EitherNec
import medeia.decoder.{BsonDecoder, BsonDecoderError}
import medeia.encoder.BsonEncoder
import org.mongodb.scala.bson.BsonValue

trait BsonCodec[A] extends BsonEncoder[A] with BsonDecoder[A]

object BsonCodec {
  def apply[A: BsonCodec]: BsonCodec[A] = implicitly

  implicit def fromEncoderAndDecoder[A: BsonEncoder: BsonDecoder](
      implicit encoder: BsonEncoder[A],
      decoder: BsonDecoder[A]): BsonCodec[A] = new BsonCodec[A] {
    override def encode(a: A): BsonValue = encoder.encode(a)

    override def decode(bson: BsonValue): EitherNec[BsonDecoderError, A] =
      decoder.decode(bson)
  }

  implicit val bsonCodecInvariant: Invariant[BsonCodec] =
    new Invariant[BsonCodec] {
      override def imap[A, B](fa: BsonCodec[A])(f: A => B)(
          g: B => A): BsonCodec[B] = new BsonCodec[B] {
        override def encode(b: B): BsonValue = fa.encode(g(b))

        override def decode(bson: BsonValue): EitherNec[BsonDecoderError, B] =
          fa.decode(bson).map(f)
      }
    }
}
