package medeia.generic

import medeia.encoder.BsonDocumentEncoder
import medeia.generic.util.VersionSpecific.Lazy
import org.mongodb.scala.bson.BsonString
import shapeless.labelled.FieldType
import shapeless.{:+:, CNil, Coproduct, Inl, Inr, Witness}

trait CoproductEncoderInstances {
  implicit def cnilEncoder[Base]: ShapelessEncoder[Base, CNil] = _ => throw new Exception("Inconceivable!")
  implicit def coproductEncoder[Base, K <: Symbol, H, T <: Coproduct](
      implicit
      witness: Witness.Aux[K],
      hEncoder: Lazy[BsonDocumentEncoder[H]],
      tEncoder: ShapelessEncoder[Base, T],
      options: SealedTraitDerivationOptions[Base] = SealedTraitDerivationOptions[Base]()
  ): ShapelessEncoder[Base, FieldType[K, H] :+: T] = {
    case Inl(head) => hEncoder.value.encode(head).append(options.discriminatorKey, BsonString(options.transformDiscriminator(witness.value.name)))
    case Inr(tail) => tEncoder.encode(tail)
  }
}
