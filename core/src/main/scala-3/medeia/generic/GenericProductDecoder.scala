package medeia.generic

import cats.data.*
import cats.syntax.either.*
import medeia.decoder.BsonDecoderError.*
import medeia.decoder.BsonDecoderError
import medeia.decoder.BsonDecoder
import medeia.decoder.StackFrame.*
import medeia.syntax.*
import org.bson.BsonString
import org.mongodb.scala.bson.{BsonDocument, BsonValue}
import medeia.decoder.BsonDecoder

import shapeless3.deriving.*

object GenericProductDecoder {
  private case class Accumulator(labels: IndexedSeq[String], errors: Chain[BsonDecoderError]) {
    def next(): Accumulator = this.copy(labels.tail)
    def next(error: BsonDecoderError) = this.copy(labels.tail, errors :+ error)
    def next(errors: NonEmptyChain[BsonDecoderError]) = this.copy(labels.tail, this.errors ++ errors.toChain)
  }

  def decoder[A](using
      inst: => K0.ProductInstances[BsonDecoder, A],
      labelling: Labelling[A],
      options: GenericDerivationOptions[A]
  ): GenericDecoder[A] = value => {
    value.fromBson[BsonDocument].flatMap(doDecode)
  }

  private def doDecode[A](bsonDocument: BsonDocument)(using
      inst: => K0.ProductInstances[BsonDecoder, A],
      labelling: Labelling[A],
      options: GenericDerivationOptions[A]
  ): EitherNec[BsonDecoderError, A] = {
    val (acc, result) = inst.unfold[Accumulator](Accumulator(labelling.elemLabels, Chain.empty))(
      [t] =>
        (acc: Accumulator, rt: BsonDecoder[t]) => {
          val label = options.transformKeys(acc.labels.head)
          val decoded = Option(bsonDocument.get(label)) match {
            case Some(headField) => rt.decode(headField).leftMap(_.map(_.push(Attr(label))))
            case None            => rt.defaultValue.toRight(NonEmptyChain(KeyNotFound(label)))
          }
          decoded match {
            case Left(error) =>
              (acc.next(error), None)
            case Right(value) =>
              (acc.next(), Some(value))
          }
      }
    )
    result match {
      case Some(value) => Right(value)
      case None =>
        Left(
          NonEmptyChain
            .fromChain(acc.errors)
            .getOrElse(NonEmptyChain(GenericDecoderError("Decoding failed but no error found (should never happen)")))
        )
    }
  }
}
