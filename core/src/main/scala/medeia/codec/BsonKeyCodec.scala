package medeia.codec

import cats.Invariant
import cats.data.EitherNec
import medeia.decoder.{BsonKeyDecoder, BsonDecoderError}
import medeia.encoder.BsonKeyEncoder

trait BsonKeyCodec[A] extends BsonKeyEncoder[A] with BsonKeyDecoder[A] { self =>
  def imap[B](f: A => B)(g: B => A): BsonKeyCodec[B] =
    new BsonKeyCodec[B] {
      override def decode(string: String): EitherNec[BsonDecoderError, B] =
        self.decode(string).map(f)

      override def encode(value: B): String = self.encode(g(value))
    }
}

object BsonKeyCodec {
  def apply[A](implicit codec: BsonKeyCodec[A]): BsonKeyCodec[A] = codec

  implicit def fromEncoderAndDecoder[A](implicit encoder: BsonKeyEncoder[A], decoder: BsonKeyDecoder[A]): BsonKeyCodec[A] =
    new BsonKeyCodec[A] {
      override def encode(a: A): String = encoder.encode(a)

      override def decode(string: String): EitherNec[BsonDecoderError, A] =
        decoder.decode(string)
    }

  implicit def invariantInstance: Invariant[BsonKeyCodec] =
    new Invariant[BsonKeyCodec] {
      override def imap[A, B](fa: BsonKeyCodec[A])(f: A => B)(g: B => A): BsonKeyCodec[B] = fa.imap(f)(g)
    }
}
