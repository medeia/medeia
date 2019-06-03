package medeia.generic

import cats.data.{EitherNec, NonEmptyChain}
import cats.instances.parallel._
import cats.syntax.either._
import cats.syntax.parallel._
import medeia.decoder.BsonDecoderError.{KeyNotFound, TypeMismatch}
import medeia.decoder.{BsonDecoder, BsonDecoderError}
import org.bson.BsonType
import org.mongodb.scala.bson.BsonDocument
import shapeless.labelled._
import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Witness}

trait GenericDecoder[A] extends BsonDecoder[A]

trait ShapelessDecoder[Base, A] {
  def decode(bsonDocument: BsonDocument): EitherNec[BsonDecoderError, A]
}

object GenericDecoder extends GenericDecoderInstances

trait GenericDecoderInstances {
  implicit def genericDecoder[A, H](
      implicit
      generic: LabelledGeneric.Aux[A, H],
      hDecoder: Lazy[ShapelessDecoder[A, H]]
  ): BsonDecoder[A] = { bson =>
    bson.getBsonType match {
      case BsonType.DOCUMENT => hDecoder.value.decode(bson.asDocument()).map(generic.from)
      case t                 => Either.leftNec(TypeMismatch(t, BsonType.DOCUMENT))
    }
  }
}
object ShapelessDecoder {
  implicit def hnilDecoder[A]: ShapelessDecoder[A, HNil] = _ => Right(HNil)

  implicit def hlistObjectDecoder[A, K <: Symbol, H, T <: HList](implicit
                                                                 witness: Witness.Aux[K],
                                                                 hDecoder: Lazy[BsonDecoder[H]],
                                                                 tDecoder: ShapelessDecoder[A, T]): ShapelessDecoder[A, FieldType[K, H] :: T] = {
    val fieldName: String = witness.value.name
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
