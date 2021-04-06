package medeia.generic

import cats.syntax.either._
import medeia.decoder.BsonDecoder
import medeia.decoder.BsonDecoderError.TypeMismatch
import medeia.generic.util.VersionSpecific.Lazy
import org.bson.BsonType
import shapeless.LabelledGeneric

trait GenericDecoder[A] extends BsonDecoder[A]

object GenericDecoder extends GenericDecoderInstances

trait GenericDecoderInstances {
  implicit def genericDecoder[Base, H](implicit
      generic: LabelledGeneric.Aux[Base, H],
      hDecoder: Lazy[ShapelessDecoder[Base, H]]
  ): GenericDecoder[Base] = { bson =>
    bson.getBsonType match {
      case BsonType.DOCUMENT => hDecoder.value.decode(bson.asDocument()).map(generic.from)
      case t                 => Either.leftNec(TypeMismatch(t, BsonType.DOCUMENT))
    }
  }
}
