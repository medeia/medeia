package medeia.generic.semiauto

import medeia.codec.BsonDocumentCodec
import medeia.decoder.BsonDecoder
import medeia.encoder.BsonDocumentEncoder
import medeia.generic.util.VersionSpecific.Lazy
import medeia.generic.{GenericDecoder, GenericEncoder}

trait Semiauto {
  @deprecated("Use BsonEncoder.derive", "0.4.2")
  def deriveBsonEncoder[A](implicit genericEncoder: Lazy[GenericEncoder[A]]): BsonDocumentEncoder[A] = genericEncoder.value

  @deprecated("Use BsonDecoder.derive", "0.4.2")
  def deriveBsonDecoder[A](implicit genericDecoder: Lazy[GenericDecoder[A]]): BsonDecoder[A] = genericDecoder.value

  @deprecated("Use BsonCodec.derive", "0.4.2")
  def deriveBsonCodec[A](implicit genericEncoder: GenericEncoder[A], genericDecoder: GenericDecoder[A]): BsonDocumentCodec[A] = {
    BsonDocumentCodec.fromEncoderAndDecoder
  }
}
