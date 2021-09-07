package medeia.enumeratum

import cats.data.EitherNec
import cats.syntax.either._
import enumeratum.values.{ValueEnum, ValueEnumEntry}
import enumeratum.{Enum, EnumEntry}
import medeia.codec.{BsonCodec, BsonKeyCodec}
import medeia.decoder.{BsonDecoder, BsonDecoderError, BsonKeyDecoder}
import medeia.decoder.BsonDecoderError.FieldParseError
import medeia.encoder.{BsonEncoder, BsonKeyEncoder}
import org.mongodb.scala.bson.BsonValue

object Enumeratum {

  def encoder[A <: EnumEntry]: BsonEncoder[A] = (value: A) => BsonEncoder.stringEncoder.encode(value.entryName)
  def decoder[A <: EnumEntry](enum: Enum[A]): BsonDecoder[A] =
    (bson: BsonValue) => BsonDecoder.stringDecoder.decode(bson).flatMap(stringToEnum(enum))

  def codec[A <: EnumEntry](enum: Enum[A]): BsonCodec[A] =
    BsonCodec.fromEncoderAndDecoder(encoder, decoder(enum))

  def keyEncoder[A <: EnumEntry]: BsonKeyEncoder[A] = (value: A) => BsonKeyEncoder.stringEncoder.encode(value.entryName)
  def keyDecoder[A <: EnumEntry](enum: Enum[A]): BsonKeyDecoder[A] = string => stringToEnum(enum)(string)

  def keyCodec[A <: EnumEntry](enum: Enum[A]): BsonKeyCodec[A] = BsonKeyCodec.fromEncoderAndDecoder(keyEncoder, keyDecoder(enum))

  def valueEnumEncoder[ValueType: BsonEncoder, EntryType <: ValueEnumEntry[ValueType]]: BsonEncoder[EntryType] =
    (a: EntryType) => BsonEncoder[ValueType].encode(a.value)

  def valueEnumDecoder[ValueType: BsonDecoder, EntryType <: ValueEnumEntry[ValueType]](
      enum: ValueEnum[ValueType, EntryType]
  ): BsonDecoder[EntryType] = (bson: BsonValue) =>
    BsonDecoder[ValueType]
      .decode(bson)
      .flatMap(value =>
        Either.catchNonFatal(enum.withValue(value)).leftMap(e => FieldParseError(s"Exception in enumeratum: ${e.getMessage}", e)).toEitherNec
      )

  def valueEnumCodec[ValueType: BsonCodec, EntryType <: ValueEnumEntry[ValueType]](
      enum: ValueEnum[ValueType, EntryType]
  ): BsonCodec[EntryType] = BsonCodec.fromEncoderAndDecoder(valueEnumEncoder, valueEnumDecoder(enum))

  private[this] def stringToEnum[A <: EnumEntry](enum: Enum[A])(string: String): EitherNec[BsonDecoderError, A] =
    Either.catchNonFatal(enum.withName(string)).leftMap(e => FieldParseError(s"Exception in enumeratum: ${e.getMessage}", e)).toEitherNec
}
