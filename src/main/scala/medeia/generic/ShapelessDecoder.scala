package medeia.generic

import cats.data.{EitherNec, NonEmptyChain}
import cats.instances.parallel._
import cats.syntax.parallel._
import medeia.decoder.BsonDecoderError.KeyNotFound
import medeia.decoder.{BsonDecoder, BsonDecoderError}
import org.mongodb.scala.bson.BsonDocument
import shapeless.labelled.{FieldType, field}
import shapeless.{::, HList, HNil, Lazy, Witness}

trait ShapelessDecoder[Base, H] {
  def decode(bsonDocument: BsonDocument): EitherNec[BsonDecoderError, H]
}

object ShapelessDecoder {
  implicit def hnilDecoder[Base]: ShapelessDecoder[Base, HNil] = _ => Right(HNil)

  implicit def hlistObjectDecoder[Base, K <: Symbol, H, T <: HList](
      implicit
      witness: Witness.Aux[K],
      hDecoder: Lazy[BsonDecoder[H]],
      tDecoder: ShapelessDecoder[Base, T],
      options: GenericDerivationOptions[Base] = GenericDerivationOptions[Base]()): ShapelessDecoder[Base, FieldType[K, H] :: T] = {
    val fieldName: String = options.transformKeys(witness.value.name)
    bsonDocument: BsonDocument =>
      {
        val head: EitherNec[BsonDecoderError, FieldType[K, H]] = Option(bsonDocument.get(fieldName)) match {
          case Some(headField) => hDecoder.value.decode(headField).map(field[K](_))
          case None            => hDecoder.value.defaultValue.map(field[K](_)).toRight(NonEmptyChain(KeyNotFound(fieldName)))
        }
        val tail = tDecoder.decode(bsonDocument)
        (head, tail).parMapN(_ :: _)
      }
  }

}
