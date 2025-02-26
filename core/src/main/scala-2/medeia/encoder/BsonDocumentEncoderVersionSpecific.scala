package medeia.encoder

import medeia.generic.GenericEncoder

trait BsonDocumentEncoderVersionSpecific {
  def derived[A](implicit genericEncoder: GenericEncoder[A]): BsonDocumentEncoder[A] = genericEncoder
}
