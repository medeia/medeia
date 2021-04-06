package medeia.codec

import cats.Invariant
import cats.data.EitherNec
import medeia.decoder.{BsonDecoder, BsonDecoderError}
import medeia.encoder.BsonDocumentEncoder
import org.bson.{BsonDocument, BsonValue}

trait BsonDocumentCodec[A] extends BsonCodec[A] with BsonDocumentEncoder[A] { self =>
  override def imap[B](f: A => B)(g: B => A): BsonDocumentCodec[B] =
    new BsonDocumentCodec[B] {
      override def decode(bson: BsonValue): EitherNec[BsonDecoderError, B] =
        self.decode(bson).map(f)

      override def encode(value: B): BsonDocument = self.encode(g(value))
    }
}

object BsonDocumentCodec {
  def apply[A](implicit codec: BsonDocumentCodec[A]): BsonDocumentCodec[A] = codec

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
