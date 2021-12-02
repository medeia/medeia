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
  def decoder[A <: EnumEntry](enumInstance: Enum[A]): BsonDecoder[A] =
    (bson: BsonValue) => BsonDecoder.stringDecoder.decode(bson).flatMap(stringToEnum(enumInstance))

  def codec[A <: EnumEntry](enumInstance: Enum[A]): BsonCodec[A] =
    BsonCodec.fromEncoderAndDecoder(encoder, decoder(enumInstance))

  def keyEncoder[A <: EnumEntry]: BsonKeyEncoder[A] = (value: A) => BsonKeyEncoder.stringEncoder.encode(value.entryName)
  def keyDecoder[A <: EnumEntry](enumInstance: Enum[A]): BsonKeyDecoder[A] = string => stringToEnum(enumInstance)(string)

  def keyCodec[A <: EnumEntry](enumInstance: Enum[A]): BsonKeyCodec[A] = BsonKeyCodec.fromEncoderAndDecoder(keyEncoder, keyDecoder(enumInstance))

  def valueEnumEncoder[ValueType: BsonEncoder, EntryType <: ValueEnumEntry[ValueType]]: BsonEncoder[EntryType] =
    (a: EntryType) => BsonEncoder[ValueType].encode(a.value)

  def valueEnumDecoder[ValueType: BsonDecoder, EntryType <: ValueEnumEntry[ValueType]](
      enumInstance: ValueEnum[ValueType, EntryType]
  ): BsonDecoder[EntryType] = (bson: BsonValue) =>
    BsonDecoder[ValueType]
      .decode(bson)
      .flatMap(value =>
        Either.catchNonFatal(enumInstance.withValue(value)).leftMap(e => FieldParseError(s"Exception in enumeratum: ${e.getMessage}", e)).toEitherNec
      )

  def valueEnumCodec[ValueType: BsonCodec, EntryType <: ValueEnumEntry[ValueType]](
      enumInstance: ValueEnum[ValueType, EntryType]
  ): BsonCodec[EntryType] = BsonCodec.fromEncoderAndDecoder(valueEnumEncoder, valueEnumDecoder(enumInstance))

  private[this] def stringToEnum[A <: EnumEntry](enumInstance: Enum[A])(string: String): EitherNec[BsonDecoderError, A] =
    Either.catchNonFatal(enumInstance.withName(string)).leftMap(e => FieldParseError(s"Exception in enumeratum: ${e.getMessage}", e)).toEitherNec
}
