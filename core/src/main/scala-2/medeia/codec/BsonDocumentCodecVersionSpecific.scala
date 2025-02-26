package medeia.codec

import medeia.decoder.BsonDecoderError
import medeia.generic.{GenericEncoder, GenericDecoder}
import cats.data.EitherNec
import org.bson.{BsonDocument, BsonValue}

trait BsonDocumentCodecVersionSpecific {
  def derived[A](implicit genericEncoder: GenericEncoder[A], genericDecoder: GenericDecoder[A]): BsonDocumentCodec[A] =
    BsonDocumentCodec.fromEncoderAndDecoder(genericEncoder, genericDecoder)
}
