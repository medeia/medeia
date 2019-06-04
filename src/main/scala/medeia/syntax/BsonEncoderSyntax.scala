package medeia.syntax

import cats.data.EitherNec
import medeia.decoder.{BsonDecoder, BsonDecoderError}
import medeia.encoder.BsonEncoder
import org.mongodb.scala.bson.BsonValue
import org.mongodb.scala.bson.collection.immutable.Document

trait BsonEncoderSyntax {
  implicit class BsonEncoderOps[A: BsonEncoder](a: A) {
    def toBson: BsonValue = BsonEncoder[A].encode(a)
  }

  implicit class BsonDecoderOps(bsonValue: BsonValue) {
    def fromBson[A: BsonDecoder]: EitherNec[BsonDecoderError, A] = BsonDecoder.decode(bsonValue)
  }

  implicit class BsonDecoderOpsForDocument(document: Document) {
    def fromBson[A: BsonDecoder]: EitherNec[BsonDecoderError, A] = BsonDecoder.decode(document.toBsonDocument)
  }
}
