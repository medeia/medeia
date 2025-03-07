package medeia.generic

import medeia.decoder.BsonDecoder
import scala.annotation.implicitNotFound

@implicitNotFound("""Could not derive BsonDecoder for ${A}.
Make sure that BsonDecoder instances exist for all fields.""")
trait GenericDecoder[A] extends BsonDecoder[A]

object GenericDecoder extends GenericDecoderInstances
