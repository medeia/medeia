package medeia.generic

import medeia.encoder.BsonDocumentEncoder
import medeia.generic.util.VersionSpecific.Lazy
import shapeless.LabelledGeneric

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
