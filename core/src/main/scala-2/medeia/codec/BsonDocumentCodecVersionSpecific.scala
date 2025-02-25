package medeia.codec

import medeia.decoder.BsonDecoderError
import medeia.generic.{GenericEncoder, GenericDecoder}
import cats.data.EitherNec
import org.bson.{BsonDocument, BsonValue}

trait BsonDocumentCodecVersionSpecific {
  def derived[A](implicit genericEncoder: GenericEncoder[A], genericDecoder: GenericDecoder[A]): BsonDocumentCodec[A] = new BsonDocumentCodec[A] {
    override def encode(a: A): BsonDocument = genericEncoder.encode(a)

    override def decode(bson: BsonValue): EitherNec[BsonDecoderError, A] =
      genericDecoder.decode(bson)
  }
}
