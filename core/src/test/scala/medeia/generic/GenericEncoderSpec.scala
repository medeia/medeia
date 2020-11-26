package medeia.generic

import medeia.MedeiaSpec
import medeia.generic.auto._
import medeia.syntax._
import org.mongodb.scala.bson.{BsonDocument, BsonInt32, BsonString}

import scala.jdk.CollectionConverters._

class GenericEncoderSpec extends MedeiaSpec {

  behavior of "GenericEncoder"

  sealed trait Trait
  case class A(string: String) extends Trait
  case class B(int: Int) extends Trait
  case class Simple(int: Int, string: String)

  //prevents unused field warnings
  object ForKeyTransformationTest {
    implicit val decoderOptions: GenericDerivationOptions[Simple] = GenericDerivationOptions { case "int" => "intA" }
  }

  it should "allow for key transformation" in {
    import ForKeyTransformationTest._

    val simple = Simple(1, "string")

    val document: BsonDocument = simple.toBsonDocument
    document.get("intA").asInt32().getValue should ===(1)
  }

  it should "encode sealed trait hierarchies" in {
    val original: Trait = A("asd")
    val document: BsonDocument = original.toBsonDocument
    document should ===(
      BsonDocument(
        "type" -> "A",
        "string" -> "asd"
      ))
  }

  it should "encode fields of a case class in the right order" in {
    val original = Simple(1, "string")

    val document = original.toBsonDocument
    document.values.asScala.toList should ===(List(BsonInt32(1), BsonString("string")))
  }

  //prevents unused field warnings
  object ForSealedTraitWithTransformationTest {
    implicit val coproductDerivationOptions: SealedTraitDerivationOptions[Trait] =
      SealedTraitDerivationOptions(discriminatorTransformation = { case a => a.toLowerCase() }, discriminatorKey = "otherType")
  }

  it should "encode sealed trait hierarchies with transformation" in {
    import medeia.generic.auto._
    import ForSealedTraitWithTransformationTest._
    val original: Trait = B(1)
    val document: BsonDocument = original.toBsonDocument
    document should ===(
      BsonDocument(
        "otherType" -> "b",
        "int" -> 1
      ))
  }

}
