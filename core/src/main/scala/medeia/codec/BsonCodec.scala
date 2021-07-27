package medeia.codec

import cats.Invariant
import cats.data.EitherNec
import medeia.codec.BsonCodec.fromEncoderAndDecoder
import medeia.decoder.{BsonDecoder, BsonDecoderError}
import medeia.encoder.BsonEncoder
import medeia.generic.{GenericDecoder, GenericEncoder}
import org.mongodb.scala.bson.BsonValue

trait BsonCodec[A] extends BsonEncoder[A] with BsonDecoder[A] { self =>
  def imap[B](f: A => B)(g: B => A): BsonCodec[B] =
    new BsonCodec[B] {
      override def decode(bson: BsonValue): EitherNec[BsonDecoderError, B] =
        self.decode(bson).map(f)

      override def encode(value: B): BsonValue = self.encode(g(value))
    }

  def iemap[B](f: A => Either[String, B])(g: B => A): BsonCodec[B] = fromEncoderAndDecoder(contramap(g), emap(f))
}

object BsonCodec {
  def apply[A](implicit codec: BsonCodec[A]): BsonCodec[A] = codec

  def derive[A](implicit genericEncoder: GenericEncoder[A], genericDecoder: GenericDecoder[A]): BsonDocumentCodec[A] = {
    BsonDocumentCodec.fromEncoderAndDecoder(genericEncoder, genericDecoder)
  }

  implicit def fromEncoderAndDecoder[A](implicit encoder: BsonEncoder[A], decoder: BsonDecoder[A]): BsonCodec[A] =
    new BsonCodec[A] {
      override def encode(a: A): BsonValue = encoder.encode(a)

      override def decode(bson: BsonValue): EitherNec[BsonDecoderError, A] =
        decoder.decode(bson)
    }

  implicit def invariantInstance: Invariant[BsonCodec] =
    new Invariant[BsonCodec] {
      override def imap[A, B](fa: BsonCodec[A])(f: A => B)(g: B => A): BsonCodec[B] = fa.imap(f)(g)
    }
}
