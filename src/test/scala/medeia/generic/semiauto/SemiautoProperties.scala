package medeia.generic.semiauto

import org.mongodb.scala.bson.BsonDocument
import org.scalacheck.{Prop, Properties}

class SemiautoProperties extends Properties("SemiAutoEncoding") {

  propertyWithSeed("decode simple case class", None) = {
    case class Simple(int: Int)
    Prop.forAll { int: Int =>
      val origin = Simple(int)
      val encoded = deriveEncoder[Simple].encode(origin)
      encoded == BsonDocument("int" -> int)
    }
  }
}
