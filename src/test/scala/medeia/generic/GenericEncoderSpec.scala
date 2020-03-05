package medeia.generic

import medeia.MedeiaSpec
import medeia.generic.auto._
import medeia.syntax._
import org.mongodb.scala.bson.BsonDocument

class GenericEncoderSpec extends MedeiaSpec {

  behavior of "GenericEncoder"

  //prevents unused field warnings
  object ForKeyTransformationTest {
    case class Simple(int: Int, string: String)
    implicit val decoderOptions: GenericDerivationOptions[Simple] = GenericDerivationOptions { case "int" => "intA" }
  }
  it should "allow for key transformation" in {
    import ForKeyTransformationTest._

    val simple = Simple(1, "string")

    val document: BsonDocument = simple.toBsonDocument
    document.get("intA").asInt32().getValue should ===(1)
  }

  //prevents unused field warnings
  object ForSealedTraitTest {
    sealed trait Trait
    case class A(string: String) extends Trait
    case class B(int: Int) extends Trait
  }

  it should "encode selead trait hierarchies" in {
    import ForSealedTraitTest._

    val original: Trait = A("asd")
    val document: BsonDocument = original.toBsonDocument
    document should ===(
      BsonDocument(
        "type" -> "A",
        "string" -> "asd"
      ))
  }

  //prevents unused field warnings
  object ForSealedTraitWithTransformationTest {
    sealed trait Trait
    case class A(string: String) extends Trait
    case class B(int: Int) extends Trait
    implicit val coproductDerivationOptions: CoproductDerivationOptions[Trait] =
      CoproductDerivationOptions(typeNameTransformation = { case a => a.toLowerCase() }, typeNameKey = "otherType")
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
