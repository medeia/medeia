package medeia.generic

import cats.data.*
import cats.syntax.either.*
import medeia.decoder.BsonDecoderError.*
import medeia.decoder.BsonDecoderError
import medeia.decoder.BsonDecoder
import medeia.decoder.StackFrame.*
import medeia.syntax.*
import org.mongodb.scala.bson.BsonDocument
import shapeless3.deriving.*
import org.bson.BsonValue

object GenericCoproductDecoder {
  def decoder[A](using
      inst: => K0.CoproductInstances[BsonDecoder, A],
      labelling: Labelling[A],
      options: SealedTraitDerivationOptions[A]
  ): GenericDecoder[A] = { value =>
    {
      for {
        document <- value.fromBson[BsonDocument]
        tagBson <- document.getSafe(options.discriminatorKey)
        tag <- tagBson.fromBson[String]
        result <- doDecode(tag, value)
      } yield (result)
    }
  }

  private def doDecode[A](discriminatorKey: String, value: BsonValue)(using
      inst: => K0.CoproductInstances[BsonDecoder, A],
      labelling: Labelling[A],
      options: SealedTraitDerivationOptions[A]
  ) = {
    labelling.elemLabels.zipWithIndex
      .map {
        case (label, index) => {
          if (discriminatorKey == options.transformDiscriminator(label)) {
            inst.inject[Option[EitherNec[BsonDecoderError, A]]](index)(
              [t <: A] => (rt: BsonDecoder[t]) => Some(rt.decode(value).leftMap(_.map(_.push(Case(discriminatorKey)))))
            )
          } else None
        }
      }
      .find(_.isDefined)
      .flatten
      .getOrElse(Left(NonEmptyChain(InvalidTypeTag(discriminatorKey))))
  }
}
