package medeia.encoder

import medeia.generic.GenericEncoder

private[medeia] trait BsonDocumentEncoderVersionSpecific {
  inline def derived[A]: BsonDocumentEncoder[A] = GenericEncoder[A]
}
