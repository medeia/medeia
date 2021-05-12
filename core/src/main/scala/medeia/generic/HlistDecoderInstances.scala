package medeia.generic

import cats.data.NonEmptyChain
import cats.syntax.either._
import cats.syntax.parallel._
import medeia.decoder.BsonDecoder
import medeia.decoder.BsonDecoderError.KeyNotFound
import medeia.decoder.StackFrame.Attr
import shapeless.labelled.{FieldType, field}
import shapeless.{::, HList, HNil, Witness}

trait HlistDecoderInstances {
  implicit def hnilDecoder[Base]: ShapelessDecoder[Base, HNil] = _ => Right(HNil)

  implicit def hlistObjectDecoder[Base, K <: Symbol, H, T <: HList](implicit
      witness: Witness.Aux[K],
      hDecoder: BsonDecoder[H],
      tDecoder: ShapelessDecoder[Base, T],
      options: GenericDerivationOptions[Base] = GenericDerivationOptions[Base]()
  ): ShapelessDecoder[Base, FieldType[K, H] :: T] =
    bsonDocument => {
      val fieldName: String = options.transformKeys(witness.value.name)

      val head = Option(bsonDocument.get(fieldName)) match {
        case Some(headField) => hDecoder.decode(headField).map(field[K](_)).leftMap(_.map(_.push(Attr(fieldName))))
        case None            => hDecoder.defaultValue.map(field[K](_)).toRight(NonEmptyChain(KeyNotFound(fieldName)))
      }
      val tail = tDecoder.decode(bsonDocument)

      (head, tail).parMapN(_ :: _)
    }
}
