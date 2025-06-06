package medeia.generic

import org.mongodb.scala.bson.BsonString
import shapeless.labelled.FieldType
import shapeless.{:+:, CNil, Coproduct, Inl, Inr, Witness}

private[medeia] trait CoproductEncoderInstances {
  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  implicit def cnilEncoder[Base]: ShapelessEncoder[Base, CNil] = (_, _) => throw new Exception("Inconceivable!")
  implicit def coproductEncoder[Base, K <: Symbol, H, T <: Coproduct](implicit
      witness: Witness.Aux[K],
      hEncoder: GenericEncoder[H],
      tEncoder: ShapelessEncoder[Base, T],
      options: GenericDerivationOptions[Base] = GenericDerivationOptions[Base]()
  ): ShapelessEncoder[Base, FieldType[K, H] :+: T] = {
    case (Inl(head), _) =>
      hEncoder.encode(head).append(options.discriminatorKey, BsonString(options.transformDiscriminator(witness.value.name)))
    case (Inr(tail), doc) => tEncoder.encode(tail, doc)
  }
}
