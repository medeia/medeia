package medeia.generic

import medeia.generic.util.VersionSpecific.Lazy
import org.mongodb.scala.bson.BsonDocument
import shapeless.LabelledGeneric

trait GenericEncoderInstances {
  implicit def genericEncoder[Base, H](implicit
      generic: LabelledGeneric.Aux[Base, H],
      hEncoder: Lazy[ShapelessEncoder[Base, H]]
  ): GenericEncoder[Base] =
    value => hEncoder.value.encode(generic.to(value), BsonDocument())
}
