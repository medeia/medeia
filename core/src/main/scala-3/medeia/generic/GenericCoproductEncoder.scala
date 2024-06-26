package medeia.generic

import medeia.encoder.BsonEncoder
import org.bson.BsonString
import org.mongodb.scala.bson.BsonDocument
import shapeless3.deriving.*

import scala.deriving.Mirror

object GenericCoproductEncoder {
  @SuppressWarnings(Array("org.wartremover.warts.SeqApply"))
  def encoder[A](using
      inst: => K0.CoproductInstances[BsonEncoder, A],
      labelling: Labelling[A],
      mirror: Mirror.SumOf[A],
      options: SealedTraitDerivationOptions[A]
  ): GenericEncoder[A] =
    (value: A) => {
      inst.fold(value)(
        [t] =>
          (st: BsonEncoder[t], t: t) =>
            val original: BsonDocument = st.encode(t).asDocument()
            val label = labelling.elemLabels(mirror.ordinal(value))
            original.append(options.discriminatorKey, BsonString(options.transformDiscriminator(label)))
      )
    }
}
