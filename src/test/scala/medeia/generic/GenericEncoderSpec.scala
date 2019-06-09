package medeia.generic

import medeia.generic.auto._
import medeia.syntax._
import org.mongodb.scala.bson.BsonDocument
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{EitherValues, FlatSpec, Matchers}

class GenericEncoderSpec extends FlatSpec with Matchers with EitherValues with TypeCheckedTripleEquals {

  "GenericEncoder" should "allow for key transformation" in {
    case class Simple(int: Int, string: String)

    val simple = Simple(1, "string")

    implicit val decoderOptions: GenericDerivationOptions[Simple] = GenericDerivationOptions { case "int" => "intA" }
    val document: BsonDocument = simple.toBson.asDocument()
    document.get("intA").asInt32().getValue should ===(1)
  }

}
