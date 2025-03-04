package medeia.codec

import cats.Invariant
import medeia.codec.BsonKeyCodec.fromEncoderAndDecoder
import medeia.decoder.{BsonDecoderError, BsonKeyDecoder}
import medeia.encoder.BsonKeyEncoder

trait BsonKeyCodec[A] extends BsonKeyEncoder[A] with BsonKeyDecoder[A] { self =>
  def imap[B](f: A => B)(g: B => A): BsonKeyCodec[B] = fromEncoderAndDecoder(contramap(g), map(f))

  def iemap[B](f: A => Either[String, B])(g: B => A): BsonKeyCodec[B] = fromEncoderAndDecoder(contramap(g), emap(f))
}

object BsonKeyCodec {
  def apply[A](implicit codec: BsonKeyCodec[A]): BsonKeyCodec[A] = codec

  implicit def fromEncoderAndDecoder[A](implicit encoder: BsonKeyEncoder[A], decoder: BsonKeyDecoder[A]): BsonKeyCodec[A] =
    new BsonKeyCodec[A] {
      override def encode(a: A): String = encoder.encode(a)

      override def decode(string: String): Either[BsonDecoderError, A] =
        decoder.decode(string)
    }

  implicit def invariantInstance: Invariant[BsonKeyCodec] =
    new Invariant[BsonKeyCodec] {
      override def imap[A, B](fa: BsonKeyCodec[A])(f: A => B)(g: B => A): BsonKeyCodec[B] = fa.imap(f)(g)
    }
}
