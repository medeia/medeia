package medeia.generic

import medeia.encoder.BsonEncoder
import org.mongodb.scala.bson.{
  BsonArray,
  BsonDocument,
  BsonInt32,
  BsonNull,
  BsonString
}
import org.scalacheck.Prop.BooleanOperators
import org.scalacheck.Prop.all
import org.scalacheck.{Gen, Prop, Properties}

class SemiautoProperties extends Properties("SemiAutoEncoding") {
  import HListEncoder._

  propertyWithSeed("decode simple case class", None) = {
    case class Simple(int: Int, string: String)
    Prop.forAll { (int: Int, string: String) =>
      val origin = Simple(int, string)
      val encoded = BsonEncoder[Simple].encode(origin)
      encoded == BsonDocument("int" -> int, "string" -> string)
    }
  }

  propertyWithSeed("decode more complex case class", None) = {
    case class Complex(int: Option[Int], stringList: List[String])
    val complexGen = for {
      intOpt <- Gen.option(Gen.posNum[Int])
      stringList <- Gen.listOf(Gen.alphaStr)
    } yield Complex(intOpt, stringList)

    Prop.forAll(complexGen) { origin =>
      val encoded = BsonEncoder[Complex].encode(origin).asDocument
      all(
        s"intOpt failed: $encoded" |:
          encoded.get("int") == origin.int
          .map(BsonInt32(_))
          .getOrElse(BsonNull()),
        s"stringList failed: $encoded ${BsonArray(origin.stringList)}" |: encoded
          .get("stringList") == BsonArray(origin.stringList.map(BsonString(_)))
      )
    }
  }

}
