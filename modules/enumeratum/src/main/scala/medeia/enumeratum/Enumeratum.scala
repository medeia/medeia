package medeia.enumeratum

import cats.syntax.either._
import enumeratum.{Enum, EnumEntry}
import medeia.codec.BsonCodec
import medeia.decoder.BsonDecoder
import medeia.decoder.BsonDecoderError.FieldParseError
import medeia.encoder.BsonEncoder
import org.mongodb.scala.bson.BsonValue

object Enumeratum {

  def encoder[A <: EnumEntry](enum: Enum[A]): BsonEncoder[A] = (value: A) => BsonEncoder.stringEncoder.encode(value.entryName)
  def decoder[A <: EnumEntry](enum: Enum[A]): BsonDecoder[A] =
    (bson: BsonValue) =>
      BsonDecoder.stringDecoder.decode(bson).flatMap { string =>
        Either.catchNonFatal(enum.withName(string)).leftMap(e => FieldParseError(s"Exception in enumeratum: ${e.getMessage}")).toEitherNec
    }

  def codec[A <: EnumEntry](enum: Enum[A]): BsonCodec[A] =
    BsonCodec.fromEncoderAndDecoder(encoder(enum), decoder(enum))

}
