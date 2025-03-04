package medeia.generic

import medeia.MedeiaSpec
import medeia.syntax._
import org.mongodb.scala.bson.{BsonDocument, BsonInt32, BsonString}

import scala.jdk.CollectionConverters._
import medeia.encoder.BsonDocumentEncoder

class GenericEncoderSpec extends MedeiaSpec {

  behavior of "GenericEncoder"

  sealed trait Trait
  case class A(string: String) extends Trait
  case object B extends Trait
  case class Simple(int: Int, string: String)

  object DefaultTraitEncoders {
    implicit val encoder: BsonDocumentEncoder[Trait] = BsonDocumentEncoder.derived
  }

  // prevents unused field warnings
  object ForKeyTransformationTest {
    implicit val encoderOptions: GenericDerivationOptions[Simple] = GenericDerivationOptions { case "int" => "intA" }
    implicit val encoder: BsonDocumentEncoder[Simple] = BsonDocumentEncoder.derived
  }

  it should "allow for key transformation" in {
    import ForKeyTransformationTest._

    val simple = Simple(1, "string")

    val document: BsonDocument = simple.toBsonDocument
    document.get("intA").asInt32().getValue should ===(1)
  }

  it should "encode sealed trait hierarchies" in {
    import DefaultTraitEncoders._
    val original: Trait = A("asd")
    val document: BsonDocument = original.toBsonDocument
    document should ===(
      BsonDocument(
        "type" -> "A",
        "string" -> "asd"
      )
    )
  }

  it should "encode case objects" in {
    import DefaultTraitEncoders._
    val original: Trait = B
    val document: BsonDocument = original.toBsonDocument
    document should ===(BsonDocument("type" -> BsonString("B")))
  }

  it should "encode fields of a case class in the right order" in {
    implicit val encoder: BsonDocumentEncoder[Simple] = BsonDocumentEncoder.derived
    val original = Simple(1, "string")

    val document = original.toBsonDocument
    document.values.asScala.toList should ===(List(BsonInt32(1), BsonString("string")))
  }

  // prevents unused field warnings
  object ForSealedTraitWithTransformationTest {
    implicit val traitDerivationOptions: GenericDerivationOptions[Trait] =
      GenericDerivationOptions(
        discriminatorTransformation = { case d => d.toLowerCase() },
        discriminatorKey = "otherType"
      )
    implicit val caseClassDerivationOptions: GenericDerivationOptions[A] =
      GenericDerivationOptions(
        keyTransformation = { case d => d.toUpperCase() }
      )
    implicit val encoder: BsonDocumentEncoder[Trait] = BsonDocumentEncoder.derived
  }

  it should "encode sealed trait hierarchies with transformation" in {
    import ForSealedTraitWithTransformationTest._
    val original: Trait = A("1")
    val document: BsonDocument = original.toBsonDocument
    document should ===(
      BsonDocument(
        "otherType" -> "a",
        "STRING" -> "1"
      )
    )
  }

}
