package medeia.generic.auto

import medeia.encoder.BsonEncoder
import org.mongodb.scala.bson.BsonDocument
import org.scalacheck.{Prop, Properties}

class AutoProperties extends Properties("AutoEncoding") {

  propertyWithSeed("decode simple case class", None) = {
    case class Simple(int: Int)
    Prop.forAll { int: Int =>
      val origin = Simple(int)
      val encoded = BsonEncoder[Simple].encode(origin)
      encoded == BsonDocument("int" -> int)
    }
  }
}
