package medeia.generic

import org.mongodb.scala.bson.BsonDocument

private[medeia] trait ShapelessEncoder[Base, H] {
  def encode(a: H, acc: BsonDocument): BsonDocument
}

private[medeia] object ShapelessEncoder extends HlistEncoderInstances with CoproductEncoderInstances
