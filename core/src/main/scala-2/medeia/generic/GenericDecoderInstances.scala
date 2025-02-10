package medeia.generic

import cats.syntax.either._
import medeia.decoder.BsonDecoderError.TypeMismatch
import org.bson.BsonType
import shapeless.LabelledGeneric

trait GenericDecoderInstances {
  implicit def genericDecoder[Base, H](implicit
      generic: LabelledGeneric.Aux[Base, H],
      hDecoder: => ShapelessDecoder[Base, H]
  ): GenericDecoder[Base] = { bson =>
    bson.getBsonType match {
      case BsonType.DOCUMENT => hDecoder.decode(bson.asDocument()).map(generic.from)
      case t                 => Either.leftNec(TypeMismatch(t, BsonType.DOCUMENT))
    }
  }
}
