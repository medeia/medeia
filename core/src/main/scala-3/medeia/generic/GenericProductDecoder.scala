package medeia.generic

import cats.data.*
import cats.syntax.either.*
import medeia.decoder.BsonDecoderError.*
import medeia.decoder.BsonDecoderError
import medeia.decoder.BsonDecoder
import medeia.decoder.StackFrame.*
import medeia.syntax.*
import org.mongodb.scala.bson.{BsonDocument}

import shapeless3.deriving.*

object GenericProductDecoder {
  @SuppressWarnings(Array("org.wartremover.warts.IterableOps"))
  private case class Accumulator(labels: IndexedSeq[String], errors: Option[NonEmptyChain[BsonDecoderError]]) {
    def discardLabel(): Accumulator = this.copy(labels.tail)
    def addErrors(errors: NonEmptyChain[BsonDecoderError]): Accumulator = this.copy(
      labels = labels.tail,
      errors = this.errors.fold(Some(errors))(es => Some(es ++ errors))
    )
  }

  def decoder[A](using
      inst: => K0.ProductInstances[BsonDecoder, A],
      labelling: Labelling[A],
      options: GenericDerivationOptions[A]
  ): GenericDecoder[A] = value => {
    value.fromBson[BsonDocument].flatMap(doDecode)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Throw", "org.wartremover.warts.AsInstanceOf"))
  private def doDecode[A](bsonDocument: BsonDocument)(using
      inst: => K0.ProductInstances[BsonDecoder, A],
      labelling: Labelling[A],
      options: GenericDerivationOptions[A]
  ): EitherNec[BsonDecoderError, A] = {
    val (acc, result) = inst.unfold[Accumulator](Accumulator(labelling.elemLabels, None))(
      [t] =>
        (acc: Accumulator, rt: BsonDecoder[t]) =>
          val label =
            acc.labels.headOption
              .map(options.transformKeys)
              .getOrElse(throw new RuntimeException("Bug in derivation logic, accumulator has no labels!"))

          val decoded = Option(bsonDocument.get(label)) match {
            case Some(headField) => rt.decode(headField).leftMap(_.map(_.push(Attr(label))))
            case None            => rt.defaultValue.toRight(NonEmptyChain(KeyNotFound(label)))
          }

          decoded match {
            case Left(es)     => (acc.addErrors(es), Some(null.asInstanceOf[t])) // we have to avoid stopping the unfold via None
            case Right(value) => (acc.discardLabel(), Some(value))
        }
    )

    (acc, result) match {
      case (Accumulator(Seq(), None), Some(r)) => Right(r)
      case (Accumulator(Seq(), Some(es)), _)   => Left(es)
      case (acc, r)                            => throw new IllegalStateException(s"Bug in derivation logic: acc=$acc, result=$r")
    }
  }
}
