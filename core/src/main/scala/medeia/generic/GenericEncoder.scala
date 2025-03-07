package medeia.generic

import medeia.encoder.BsonDocumentEncoder

import scala.annotation.implicitNotFound

@implicitNotFound("""Could not derive BsonDocumentEncoder for ${A}.
Make sure that BsonEncoder instances exist for all fields.""")
trait GenericEncoder[A] extends BsonDocumentEncoder[A]

object GenericEncoder extends GenericEncoderInstances
