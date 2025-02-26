package medeia.decoder

import medeia.generic.GenericDecoder

private[medeia] trait BsonDecoderVersionSpecific {
  inline def derived[A]: BsonDecoder[A] = GenericDecoder[A]
}
