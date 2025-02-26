package medeia.codec

import medeia.generic.{GenericEncoder, GenericDecoder}

private[medeia] trait BsonDocumentCodecVersionSpecific {
  inline def derived[A]: BsonDocumentCodec[A] = BsonDocumentCodec.fromEncoderAndDecoder(GenericEncoder[A], GenericDecoder[A])
}
