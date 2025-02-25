package medeia.codec

import medeia.decoder.BsonDecoderError
import medeia.generic.{GenericEncoder, GenericDecoder}
import cats.data.EitherNec
import org.bson.{BsonDocument, BsonValue}

trait BsonDocumentCodecVersionSpecific {
    inline def derived[A]: BsonDocumentCodec[A] = new BsonDocumentCodec[A] {
      override def encode(a: A): BsonDocument = GenericEncoder[A].encode(a)

      override def decode(bson: BsonValue): EitherNec[BsonDecoderError, A] =
        GenericDecoder[A].decode(bson)
  }
}
