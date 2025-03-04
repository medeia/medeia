package medeia.syntax

import medeia.decoder.BsonDecoderError.KeyNotFound
import medeia.decoder.{BsonDecoder, BsonDecoderError}
import medeia.encoder.{BsonDocumentEncoder, BsonEncoder}
import org.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import org.mongodb.scala.bson.collection.immutable.Document

trait MedeiaSyntax {
  implicit class MedeiaOps[A](value: A) {
    def toBson(implicit encoder: BsonEncoder[A]): BsonValue = encoder.encode(value)
    def toBsonDocument(implicit encoder: BsonDocumentEncoder[A]): BsonDocument = encoder.encode(value)
  }

  implicit class BsonDecoderOps(bsonValue: BsonValue) {
    def fromBson[A: BsonDecoder]: Either[BsonDecoderError, A] = BsonDecoder.decode(bsonValue)
  }

  implicit class BsonDecoderOpsForDocument(document: Document) {
    def fromBson[A: BsonDecoder]: Either[BsonDecoderError, A] = BsonDecoder.decode(document.toBsonDocument)
  }

  implicit class GetSafeOpsForDocument(document: Document) {
    def getSafe(key: String): Either[BsonDecoderError, BsonValue] = document.get(key).toRight(KeyNotFound(key))
  }

  implicit class GetSafeOpsForBsonDocument(document: BsonDocument) {
    def getSafe(key: String): Either[BsonDecoderError, BsonValue] = Option(document.get(key)).toRight(KeyNotFound(key))
  }
}
