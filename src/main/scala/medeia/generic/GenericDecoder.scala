package medeia.generic

import cats.syntax.either._
import medeia.decoder.BsonDecoder
import medeia.decoder.BsonDecoderError.TypeMismatch
import org.bson.BsonType
import org.mongodb.scala.bson.BsonDocument
import shapeless.labelled._
import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Witness}

trait GenericDecoder[A] extends BsonDecoder[A]

trait ShapelessDecoder[Base, A] {
  def decode(bsonDocument: BsonDocument): A
}

object GenericDecoder extends GenericDecoderInstances

trait GenericDecoderInstances {
  implicit def genericDecoder[A, H](
      implicit
      generic: LabelledGeneric.Aux[A, H],
      hDecoder: Lazy[ShapelessDecoder[A, H]]
  ): BsonDecoder[A] = { bson =>
    bson.getBsonType match {
      case BsonType.DOCUMENT => Right(generic.from(hDecoder.value.decode(bson.asDocument())))
      case t                 => Either.leftNec(TypeMismatch(t, BsonType.DOCUMENT))
    }
  }
}
object ShapelessDecoder {
  implicit def hnilDecoder[A]: ShapelessDecoder[A, HNil] = _ => HNil

  implicit def hlistObjectDecoder[A, K <: Symbol, H, T <: HList](implicit
                                                                 witness: Witness.Aux[K],
                                                                 hDecoder: Lazy[BsonDecoder[H]],
                                                                 tDecoder: ShapelessDecoder[A, T]): ShapelessDecoder[A, FieldType[K, H] :: T] = {
    val fieldName: String = witness.value.name
    bsonDocument: BsonDocument =>
      field[K](hDecoder.value.decode(bsonDocument.get(fieldName)).right.get) :: tDecoder.decode(bsonDocument)
  }

}
