package medeia.generic

import cats.data.NonEmptyChain
import medeia.decoder.BsonDecoderError.InvalidTypeTag
import medeia.decoder.{BsonDecoder, BsonDecoderError}
import medeia.generic.util.VersionSpecific.Lazy
import medeia.syntax._
import shapeless.labelled.{FieldType, field}
import shapeless.{:+:, CNil, Coproduct, Inl, Inr, Witness}

trait CoproductDecoderInstances {
  implicit def cnilDecoder[Base](
      implicit options: SealedTraitDerivationOptions[Base] = SealedTraitDerivationOptions[Base]()): ShapelessDecoder[Base, CNil] = { bsonDocument =>
    val typeTag = bsonDocument.getSafe(options.typeTag).flatMap(_.fromBson[String])
    typeTag match {
      case Left(value)  => Left(value)
      case Right(value) => Left(NonEmptyChain(InvalidTypeTag(value)))
    }
  }

  implicit def coproductDecoder[Base, K <: Symbol, H, T <: Coproduct](
      implicit
      witness: Witness.Aux[K],
      hInstance: Lazy[BsonDecoder[H]],
      tInstance: ShapelessDecoder[Base, T],
      options: SealedTraitDerivationOptions[Base] = SealedTraitDerivationOptions[Base]()
  ): ShapelessDecoder[Base, FieldType[K, H] :+: T] = { bsonDocument =>
    val instanceTypeTag = options.transformTypeNames(witness.value.name)
    def doDecode(typeTag: String): Either[NonEmptyChain[BsonDecoderError], FieldType[K, H] :+: T] = {
      if (typeTag == instanceTypeTag) {
        hInstance.value.decode(bsonDocument).map((x: H) => Inl(field[K](x)))
      } else {
        tInstance.decode(bsonDocument).map(Inr(_))
      }
    }
    for {
      typeField <- bsonDocument.getSafe(options.typeTag)
      typeTag <- typeField.fromBson[String]
      result <- doDecode(typeTag)
    } yield result
  }
}
