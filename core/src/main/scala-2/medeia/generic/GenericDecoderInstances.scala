package medeia.generic

import medeia.decoder.BsonDecoderError.TypeMismatch
import org.bson.BsonType
import shapeless.LabelledGeneric

private[medeia] trait GenericDecoderInstances {
  implicit def genericDecoder[Base, H](implicit
      generic: LabelledGeneric.Aux[Base, H],
      hDecoder: => ShapelessDecoder[Base, H]
  ): GenericDecoder[Base] = { bson =>
    bson.getBsonType match {
      case BsonType.DOCUMENT => hDecoder.decode(bson.asDocument()).map(generic.from)
      case t                 => Left(TypeMismatch(t, BsonType.DOCUMENT))
    }
  }
}
