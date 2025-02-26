package medeia.generic

import shapeless3.deriving.K0.*
import shapeless3.deriving.*
import org.mongodb.scala.bson.*
import scala.compiletime.*
import medeia.encoder.BsonEncoder
import scala.deriving.Mirror

private[medeia] trait GenericEncoderInstances {
  inline def apply[A]: GenericEncoder[A] =
    summonInline[GenericEncoder[A]]

  @SuppressWarnings(Array("org.wartremover.warts.SeqApply"))
  given coproduct[A](using
      inst: => K0.CoproductInstances[ProductEncoder, A],
      labelling: Labelling[A],
      mirror: Mirror.SumOf[A],
      options: GenericDerivationOptions[A] = GenericDerivationOptions[A]()
  ): GenericEncoder[A] =
    (value: A) => {
      inst.fold(value)(
        [t] =>
          (st: ProductEncoder[t], t: t) =>
            val original: BsonDocument = st.encode(t).asDocument()
            val label = labelling.elemLabels(mirror.ordinal(value))
            original.append(options.discriminatorKey, BsonString(options.transformDiscriminator(label)))
      )
    }

  given product[A: Labelling](using
      inst: => ProductInstances[BsonEncoder, A],
      options: GenericDerivationOptions[A] = GenericDerivationOptions[A]()
  ): GenericEncoder[A] = ProductEncoder.product
}

private trait ProductEncoder[A] extends GenericEncoder[A]

private object ProductEncoder {
  given product[A](using
      inst: => ProductInstances[BsonEncoder, A],
      labelling: Labelling[A],
      options: GenericDerivationOptions[A] = GenericDerivationOptions[A]()
  ): ProductEncoder[A] = (value: A) =>
    BsonDocument(
      labelling.elemLabels.zipWithIndex.map((label, i) =>
        val fieldName = options.transformKeys(label)
        fieldName -> inst.project(value)(i)([t] => (encoder: BsonEncoder[t], pt: t) => encoder.encode(pt))
      )
    )
}
