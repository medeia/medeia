package medeia.generic

import org.mongodb.scala.bson.BsonDocument
import shapeless.LabelledGeneric

private[medeia] trait GenericEncoderInstances {
  implicit def genericEncoder[Base, H](implicit
      generic: LabelledGeneric.Aux[Base, H],
      hEncoder: => ShapelessEncoder[Base, H]
  ): GenericEncoder[Base] =
    value => hEncoder.encode(generic.to(value), BsonDocument())
}
