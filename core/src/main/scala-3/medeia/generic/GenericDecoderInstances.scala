package medeia.generic

import cats.syntax.either.*
import shapeless3.deriving.K0.*
import shapeless3.deriving.*
import org.mongodb.scala.bson.*
import scala.compiletime.*
import medeia.decoder.BsonDecoderError.*
import medeia.decoder.BsonDecoderError
import medeia.decoder.BsonDecoder
import medeia.decoder.StackFrame.*
import medeia.syntax.*

private[medeia] trait GenericDecoderInstances {
  inline def apply[A]: GenericDecoder[A] =
    summonInline[GenericDecoder[A]]

  given coproduct[A](using
      inst: => K0.CoproductInstances[ProductDecoder, A],
      labelling: Labelling[A],
      options: GenericDerivationOptions[A] = GenericDerivationOptions[A]()
  ): GenericDecoder[A] = { value =>
    for {
      document <- value.fromBson[BsonDocument]
      tagBson <- document.getSafe(options.discriminatorKey)
      tag <- tagBson.fromBson[String]
      result <- doDecode(tag, value)
    } yield (result)
  }

  given product[A: Labelling](using
      inst: => ProductInstances[BsonDecoder, A],
      options: GenericDerivationOptions[A] = GenericDerivationOptions[A]()
  ): GenericDecoder[A] = ProductDecoder.product

  private def doDecode[A](discriminatorKey: String, value: BsonValue)(using
      inst: => K0.CoproductInstances[ProductDecoder, A],
      labelling: Labelling[A],
      options: GenericDerivationOptions[A]
  ) = {
    labelling.elemLabels.zipWithIndex
      .map { case (label, index) =>
        Option.when(discriminatorKey == options.transformDiscriminator(label)) {
          inst.inject[Either[BsonDecoderError, A]](index)([t <: A] =>
            (rt: ProductDecoder[t]) => rt.decode(value).leftMap(_.push(Case(discriminatorKey)))
          )
        }
      }
      .collectFirst { case Some(x) => x }
      .getOrElse(Left(InvalidTypeTag(discriminatorKey)))
  }

}

private trait ProductDecoder[A] extends GenericDecoder[A]

private object ProductDecoder {
  given product[A](using
      inst: => K0.ProductInstances[BsonDecoder, A],
      labelling: Labelling[A],
      options: GenericDerivationOptions[A] = GenericDerivationOptions[A]()
  ): ProductDecoder[A] = value => {
    value.fromBson[BsonDocument].flatMap(doDecode)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Throw", "org.wartremover.warts.OptionPartial", "org.wartremover.warts.SeqApply"))
  private def doDecode[A](bsonDocument: BsonDocument)(using
      inst: => K0.ProductInstances[BsonDecoder, A],
      labelling: Labelling[A],
      options: GenericDerivationOptions[A]
  ): Either[BsonDecoderError, A] = {
    val (acc, result) = inst.unfold[Either[BsonDecoderError, Int]](Right(0))([t] =>
      (acc: Either[BsonDecoderError, Int], rt: BsonDecoder[t]) =>
        val index = acc.toOption.get // guaranteed to be right because Left stops the unfold
        val label = options.transformKeys(labelling.elemLabels(index)) // guaranteed to be present

        val decoded = Option(bsonDocument.get(label)) match {
          case Some(headField) => rt.decode(headField).leftMap(_.push(Attr(label)))
          case None            => rt.defaultValue.toRight(KeyNotFound(label))
        }

        decoded match {
          case Left(e)      => (Left(e), None)
          case Right(value) => (Right(index + 1), Some(value))
        }
    )

    (acc, result) match {
      case (Left(e), _) => Left(e)
      case (_, Some(r)) => Right(r)
      case (acc, r)     => throw new IllegalStateException(s"Bug in derivation logic: acc=$acc, result=$r")
    }
  }
}
