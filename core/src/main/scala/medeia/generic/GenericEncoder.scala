package medeia.generic

import medeia.encoder.BsonDocumentEncoder

trait GenericEncoder[A] extends BsonDocumentEncoder[A]

object GenericEncoder extends GenericEncoderInstances
