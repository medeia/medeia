package medeia.generic

import medeia.encoder.{BsonDocumentEncoder, BsonEncoder}
import org.mongodb.scala.bson.BsonDocument
import shapeless.labelled.FieldType
import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Witness}

trait GenericEncoder[-A] extends BsonDocumentEncoder[A]

object GenericEncoder extends GenericEncoderInstances

trait GenericEncoderInstances {
  implicit def genericEncoder[A, H](
      implicit
      generic: LabelledGeneric.Aux[A, H],
      hEncoder: Lazy[GenericEncoder[H]]
  ): GenericEncoder[A] =
    value => hEncoder.value.encode(generic.to(value))

  implicit val hnilEncoder: GenericEncoder[HNil] =
    hnil => BsonDocument()

  implicit def hlistObjectEncoder[K <: Symbol, H, T <: HList](
      implicit
      witness: Witness.Aux[K],
      hEncoder: Lazy[BsonEncoder[H]],
      tEncoder: GenericEncoder[T]): GenericEncoder[FieldType[K, H] :: T] = {
    val fieldName: String = witness.value.name
    hlist =>
      {
        val head = hEncoder.value.encode(hlist.head)
        val tail: BsonDocument = tEncoder.encode(hlist.tail)
        tail.append(fieldName, head)
      }
  }
}
