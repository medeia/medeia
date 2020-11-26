package medeia.generic

import org.mongodb.scala.bson.BsonDocument

trait ShapelessEncoder[Base, H] {
  def encode(a: H, acc: BsonDocument): BsonDocument
}

object ShapelessEncoder extends HlistEncoderInstances with CoproductEncoderInstances
