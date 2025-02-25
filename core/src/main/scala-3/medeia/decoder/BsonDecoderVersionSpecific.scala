package medeia.decoder

import medeia.generic.GenericDecoder

trait BsonDecoderVersionSpecific {
    inline def derived[A]: BsonDecoder[A] = GenericDecoder[A]
}
