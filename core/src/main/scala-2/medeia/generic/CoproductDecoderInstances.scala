package medeia.generic

import cats.data.NonEmptyChain
import cats.syntax.eq._
import cats.syntax.either._
import medeia.decoder.BsonDecoderError.InvalidTypeTag
import medeia.decoder.StackFrame.Case
import medeia.decoder.BsonDecoderError
import medeia.syntax._
import shapeless.labelled.{FieldType, field}
import shapeless.{:+:, CNil, Coproduct, Inl, Inr, Witness}

private[medeia] trait CoproductDecoderInstances {
  implicit def cnilDecoder[Base](implicit
      options: GenericDerivationOptions[Base] = GenericDerivationOptions[Base]()
  ): ShapelessDecoder[Base, CNil] = { bsonDocument =>
    val typeTag = bsonDocument.getSafe(options.discriminatorKey).flatMap(_.fromBson[String])
    typeTag match {
      case Left(value)  => Left(value)
      case Right(value) => Left(NonEmptyChain(InvalidTypeTag(value)))
    }
  }

  implicit def coproductDecoder[Base, K <: Symbol, H, T <: Coproduct](implicit
      witness: Witness.Aux[K],
      hInstance: GenericDecoder[H],
      tInstance: ShapelessDecoder[Base, T],
      options: GenericDerivationOptions[Base] = GenericDerivationOptions[Base]()
  ): ShapelessDecoder[Base, FieldType[K, H] :+: T] = { bsonDocument =>
    val instanceDiscriminator = options.transformDiscriminator(witness.value.name)

    def doDecode(discriminatorFromBson: String): Either[NonEmptyChain[BsonDecoderError], FieldType[K, H] :+: T] = {
      if (discriminatorFromBson === instanceDiscriminator) {
        hInstance.decode(bsonDocument).map((x: H) => Inl(field[K](x))).leftMap(_.map(_.push(Case(instanceDiscriminator))))
      } else {
        tInstance.decode(bsonDocument).map(Inr(_))
      }
    }

    for {
      discriminatorField <- bsonDocument.getSafe(options.discriminatorKey)
      discriminatorFromBson <- discriminatorField.fromBson[String]
      result <- doDecode(discriminatorFromBson)
    } yield result
  }
}
