package medeia.generic

import medeia.encoder.BsonDocumentEncoder
import shapeless.{LabelledGeneric, Lazy}

trait GenericEncoder[A] extends BsonDocumentEncoder[A]

object GenericEncoder extends GenericEncoderInstances

trait GenericEncoderInstances {
  implicit def genericEncoder[Base, H](
      implicit
      generic: LabelledGeneric.Aux[Base, H],
      hEncoder: Lazy[ShapelessEncoder[Base, H]]
  ): GenericEncoder[Base] =
    value => hEncoder.value.encode(generic.to(value))
}
