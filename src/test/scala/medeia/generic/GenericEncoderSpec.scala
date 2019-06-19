package medeia.generic

import medeia.MedeiaSpec
import medeia.generic.auto._
import medeia.syntax._
import org.mongodb.scala.bson.BsonDocument

class GenericEncoderSpec extends MedeiaSpec {

  "GenericEncoder" should "allow for key transformation" in {
    case class Simple(int: Int, string: String)

    val simple = Simple(1, "string")

    implicit val decoderOptions: GenericDerivationOptions[Simple] = GenericDerivationOptions { case "int" => "intA" }
    val document: BsonDocument = simple.toBson.asDocument()
    document.get("intA").asInt32().getValue should ===(1)
  }

}
