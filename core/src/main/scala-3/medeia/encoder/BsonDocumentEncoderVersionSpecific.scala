package medeia.encoder

import medeia.generic.GenericEncoder

trait BsonDocumentEncoderVersionSpecific {
  inline def derived[A]: BsonDocumentEncoder[A] = GenericEncoder[A]
}
