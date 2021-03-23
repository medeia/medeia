package medeia.generic

import medeia.encoder.BsonDocumentEncoder
import medeia.generic.util.VersionSpecific.Lazy
import org.mongodb.scala.bson.BsonString
import shapeless.labelled.FieldType
import shapeless.{:+:, CNil, Coproduct, Inl, Inr, Witness}

trait CoproductEncoderInstances {
  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  implicit def cnilEncoder[Base]: ShapelessEncoder[Base, CNil] = (_, _) => throw new Exception("Inconceivable!")
  implicit def coproductEncoder[Base, K <: Symbol, H, T <: Coproduct](
      implicit
      witness: Witness.Aux[K],
      hEncoder: Lazy[BsonDocumentEncoder[H]],
      tEncoder: ShapelessEncoder[Base, T],
      options: SealedTraitDerivationOptions[Base] = SealedTraitDerivationOptions[Base]()
  ): ShapelessEncoder[Base, FieldType[K, H] :+: T] = {
    case (Inl(head), _) =>
      hEncoder.value.encode(head).append(options.discriminatorKey, BsonString(options.transformDiscriminator(witness.value.name)))
    case (Inr(tail), doc) => tEncoder.encode(tail, doc)
  }
}
