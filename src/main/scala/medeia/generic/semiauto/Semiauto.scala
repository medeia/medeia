package medeia.generic.semiauto

import medeia.codec.BsonDocumentCodec
import medeia.decoder.BsonDecoder
import medeia.encoder.BsonDocumentEncoder
import medeia.generic.{GenericDecoder, GenericEncoder}
import shapeless.Lazy

trait Semiauto {
  def deriveBsonEncoder[A](implicit genericEncoder: Lazy[GenericEncoder[A]]): BsonDocumentEncoder[A] = genericEncoder.value

  def deriveBsonDecoder[A](implicit genericDecoder: Lazy[GenericDecoder[A]]): BsonDecoder[A] = genericDecoder.value

  def deriveBsonCodec[A](implicit
                         genericEncoder: GenericEncoder[A],
                         genericDecoder: GenericDecoder[A]): BsonDocumentCodec[A] = {
    BsonDocumentCodec.fromEncoderAndDecoder
  }
}
