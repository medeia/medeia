package medeia.codec

import cats.Invariant
import cats.data.EitherNec
import medeia.codec.BsonDocumentCodec.fromEncoderAndDecoder
import medeia.decoder.{BsonDecoder, BsonDecoderError}
import medeia.encoder.BsonDocumentEncoder
import medeia.generic.{GenericEncoder, GenericDecoder}
import org.bson.{BsonDocument, BsonValue}

trait BsonDocumentCodec[A] extends BsonCodec[A] with BsonDocumentEncoder[A] {
  override def imap[B](f: A => B)(g: B => A): BsonDocumentCodec[B] = fromEncoderAndDecoder(contramap(g), map(f))

  override def iemap[B](f: A => Either[String, B])(g: B => A): BsonDocumentCodec[B] = fromEncoderAndDecoder(contramap(g), emap(f))
}

object BsonDocumentCodec {
  def apply[A](implicit codec: BsonDocumentCodec[A]): BsonDocumentCodec[A] = codec

  def derived[A](implicit genericEncoder: GenericEncoder[A], genericDecoder: GenericDecoder[A]): BsonDocumentCodec[A] = {
    BsonDocumentCodec.fromEncoderAndDecoder(genericEncoder, genericDecoder)
  }

  implicit def fromEncoderAndDecoder[A](implicit encoder: BsonDocumentEncoder[A], decoder: BsonDecoder[A]): BsonDocumentCodec[A] =
    new BsonDocumentCodec[A] {
      override def encode(a: A): BsonDocument = encoder.encode(a)

      override def decode(bson: BsonValue): EitherNec[BsonDecoderError, A] =
        decoder.decode(bson)
    }

  implicit def invariantInstance: Invariant[BsonDocumentCodec] =
    new Invariant[BsonDocumentCodec] {
      override def imap[A, B](fa: BsonDocumentCodec[A])(f: A => B)(g: B => A): BsonDocumentCodec[B] = fa.imap(f)(g)
    }
}
