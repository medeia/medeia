package medeia.generic

import medeia.decoder.BsonDecoder

import org.mongodb.scala.bson.{BsonDocument, BsonValue}
import shapeless3.deriving.*

import scala.deriving.Mirror
trait GenericDecoderInstances {
  implicit inline def genericDecoder[A](using
      gen: K0.Generic[A],
      gdOptions: GenericDerivationOptions[A] = GenericDerivationOptions[A](),
      stOptions: SealedTraitDerivationOptions[A] = SealedTraitDerivationOptions[A]()
  ): GenericDecoder[A] =
    gen.derive(GenericProductDecoder.decoder, GenericCoproductDecoder.decoder)
}
