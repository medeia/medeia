package medeia.generic

import medeia.encoder.{BsonDocumentEncoder, BsonEncoder}
import org.mongodb.scala.bson.BsonDocument
import shapeless.labelled.FieldType
import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Witness}

object Semiauto {}

object HListEncoder {
  implicit def genericObjectEncoder[A, H](
      implicit
      generic: LabelledGeneric.Aux[A, H],
      hEncoder: Lazy[BsonDocumentEncoder[H]]
  ): BsonEncoder[A] =
    value => hEncoder.value.encode(generic.to(value))

  implicit val hnilEncoder: BsonDocumentEncoder[HNil] =
    hnil => BsonDocument()

  implicit def hlistObjectEncoder[K <: Symbol, H, T <: HList](
      implicit
      witness: Witness.Aux[K],
      hEncoder: Lazy[BsonEncoder[H]],
      tEncoder: BsonDocumentEncoder[T])
    : BsonDocumentEncoder[FieldType[K, H] :: T] = {
    val fieldName: String = witness.value.name
    hlist =>
      {
        val head = hEncoder.value.encode(hlist.head)
        val tail: BsonDocument = tEncoder.encode(hlist.tail)
        tail.append(fieldName, head)
      }
  }
}
