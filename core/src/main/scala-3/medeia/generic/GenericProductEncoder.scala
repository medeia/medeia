package medeia.generic

import medeia.encoder.BsonEncoder
import org.mongodb.scala.bson.BsonDocument

import shapeless3.deriving.*

object GenericProductEncoder {
  def encoder[A](using
      inst: => K0.ProductInstances[BsonEncoder, A],
      labelling: Labelling[A],
      options: GenericDerivationOptions[A]
  ): GenericEncoder[A] =
    (value: A) =>
      BsonDocument(
        labelling.elemLabels.zipWithIndex.map((label, i) => {
          val fieldName = options.transformKeys(label)
          fieldName -> inst.project(value)(i)([t] => (encoder: BsonEncoder[t], pt: t) => encoder.encode(pt))
        })
      )
}
