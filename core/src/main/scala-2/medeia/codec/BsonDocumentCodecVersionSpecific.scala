package medeia.codec

import medeia.generic.{GenericEncoder, GenericDecoder}

trait BsonDocumentCodecVersionSpecific {
  def derived[A](implicit genericEncoder: GenericEncoder[A], genericDecoder: GenericDecoder[A]): BsonDocumentCodec[A] =
    BsonDocumentCodec.fromEncoderAndDecoder(genericEncoder, genericDecoder)
}
