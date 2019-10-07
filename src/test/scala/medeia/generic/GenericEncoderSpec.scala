package medeia.generic

import medeia.MedeiaSpec
import medeia.encoder.BsonEncoder
import medeia.generic.auto._
import medeia.syntax._
import org.mongodb.scala.bson.BsonDocument
import shapeless.{:+:, CNil, HList, LabelledGeneric}

class GenericEncoderSpec extends MedeiaSpec {

  "GenericEncoder" should "allow for key transformation" in {
    case class Simple(int: Int, string: String)

    val simple = Simple(1, "string")

    implicit val decoderOptions: GenericDerivationOptions[Simple] = GenericDerivationOptions { case "int" => "intA" }
    val document: BsonDocument = simple.toBson.asDocument()
    document.get("intA").asInt32().getValue should ===(1)
  }

  it should "encode trait hierarchies" in {
    sealed trait Trait
    case class A(string: String) extends Trait
    case class B(int: Int) extends Trait

    val original: Trait = A("asd")
    val traitEncoder: BsonEncoder[Trait] = GenericEncoder.genericEncoder
    val document: BsonDocument = traitEncoder.encode(original).asDocument()
    document should ===(
      BsonDocument(
        "type" -> "A",
        "string" -> "asd"
      ))
  }

}
