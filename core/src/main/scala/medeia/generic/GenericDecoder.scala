package medeia.generic

import medeia.decoder.BsonDecoder

trait GenericDecoder[A] extends BsonDecoder[A]

object GenericDecoder extends GenericDecoderInstances
