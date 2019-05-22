package medeia.syntax

import medeia.encoder.BsonEncoder
import org.mongodb.scala.bson.BsonValue

trait BsonEncoderSyntax {
  implicit class BsonEncoderOps[A: BsonEncoder](a: A) {
    def toBson: BsonValue = BsonEncoder[A].encode(a)
  }
}
