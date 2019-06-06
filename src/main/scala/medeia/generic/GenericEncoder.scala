package medeia.generic

import medeia.encoder.{BsonDocumentEncoder, BsonEncoder}
import org.mongodb.scala.bson.BsonDocument
import shapeless.labelled.FieldType
import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Witness}

trait GenericEncoder[A] extends BsonDocumentEncoder[A]

trait ShapelessEncoder[Base, H] {
  def encode(a: H): BsonDocument
}

object GenericEncoder extends GenericEncoderInstances

trait GenericEncoderInstances {
  implicit def genericEncoder[Base, H](
      implicit
      options: GenericEncodingOptions[Base] = GenericEncodingOptions[Base](),
      generic: LabelledGeneric.Aux[Base, H],
      hEncoder: Lazy[ShapelessEncoder[Base, H]]
  ): GenericEncoder[Base] =
    value => hEncoder.value.encode(generic.to(value))
}

object ShapelessEncoder {
  implicit def hnilEncoder[Base]: ShapelessEncoder[Base, HNil] =
    hnil => BsonDocument()

  implicit def hlistObjectEncoder[Base, K <: Symbol, H, T <: HList](
      implicit
      witness: Witness.Aux[K],
      hEncoder: Lazy[BsonEncoder[H]],
      tEncoder: ShapelessEncoder[Base, T],
      options: GenericEncodingOptions[Base] = GenericEncodingOptions[Base]()
  ): ShapelessEncoder[Base, FieldType[K, H] :: T] = {
    val fieldName: String = options.transformKeys(witness.value.name)
    hlist =>
      {
        val head = hEncoder.value.encode(hlist.head)
        val tail: BsonDocument = tEncoder.encode(hlist.tail)
        tail.append(fieldName, head)
      }
  }
}
