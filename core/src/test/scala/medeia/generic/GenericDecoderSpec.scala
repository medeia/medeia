package medeia.generic

import medeia.MedeiaSpec
import medeia.decoder.BsonDecoder
import medeia.decoder.BsonDecoderError.{InvalidTypeTag, KeyNotFound, TypeMismatch}
import medeia.decoder.ErrorStack
import medeia.decoder.StackFrame.{Attr, Case, Index}
import medeia.syntax._
import org.bson.BsonType
import org.mongodb.scala.bson.{BsonArray, BsonBoolean, BsonDocument, BsonNull}

class GenericDecoderSpec extends MedeiaSpec {

  sealed trait Trait
  case class A(string: String) extends Trait
  case class B(int: Int) extends Trait

  "GenericDecoder" should "handle errors" in {
    case class Simple(int: Int, string: String)
    implicit val decoder: BsonDecoder[Simple] = BsonDecoder.derived
    val doc = BsonDocument()
    doc.fromBson[Simple] should ===(Left(KeyNotFound("int")))
  }

  it should "decode empty values to None" in {
    case class Simple(int: Option[Int])
    implicit val decoder: BsonDecoder[Simple] = BsonDecoder.derived
    val doc = BsonDocument()
    doc.fromBson[Simple] should ===(Right(Simple(None)))
  }

  it should "fail gracefully on nested decoders" in {
    case class Inner(int: Int)
    implicit val innerdecoder: BsonDecoder[Inner] = BsonDecoder.derived
    case class Outer(inner: Inner)
    implicit val outerdecoder: BsonDecoder[Outer] = BsonDecoder.derived
    val doc = BsonDocument(List("inner" -> BsonNull()))
    doc.fromBson[Outer] should ===(Left(TypeMismatch(BsonType.NULL, BsonType.DOCUMENT, ErrorStack(List(Attr("inner"))))))
  }

  it should "allow for key transformation" in {
    case class Simple(int: Int, string: String)
    implicit val derivationOptions: GenericDerivationOptions[Simple] = GenericDerivationOptions { case "int" => "intA" }
    implicit val decoder: BsonDecoder[Simple] = BsonDecoder.derived[Simple]
    val doc = BsonDocument(
      "intA" -> 1,
      "string" -> "string"
    )
    doc.fromBson[Simple] should ===(Right(Simple(1, "string")))
  }

  it should "decode sealed trait hierarchies" in {
    implicit val decoder: BsonDecoder[Trait] = BsonDecoder.derived
    val original = BsonDocument(
      "type" -> "B",
      "int" -> 1
    )

    val result = original.fromBson[Trait]
    result should ===(Right(B(1)))
  }

  // prevents unused field warnings
  object ForSealedTraitWithTransformationTest {
    implicit val coproductDerivationOptions: GenericDerivationOptions[Trait] =
      GenericDerivationOptions(discriminatorTransformation = { case d => d.toLowerCase() }, discriminatorKey = "otherType")
    implicit val decoder: BsonDecoder[Trait] = BsonDecoder.derived
  }

  it should "decode sealed trait hierarchies with transformation" in {
    import ForSealedTraitWithTransformationTest._
    val original = BsonDocument(
      "otherType" -> "b",
      "int" -> 1
    )

    val result = original.fromBson[Trait]
    result should ===(Right(B(1)))
  }

  it should "fail on unknown discriminator" in {
    implicit val decoder: BsonDecoder[Trait] = BsonDecoder.derived
    val original = BsonDocument(
      "type" -> "Z",
      "int" -> 1
    )

    val result = original.fromBson[Trait]
    result should ===(Left(InvalidTypeTag("Z")))
  }

  it should "fail on missing discriminator" in {
    implicit val decoder: BsonDecoder[Trait] = BsonDecoder.derived
    val original = BsonDocument(
      "int" -> 1
    )

    val result = original.fromBson[Trait]
    result should ===(Left(KeyNotFound("type")))
  }

  it should "fail on invalid discriminator key" in {
    implicit val decoder: BsonDecoder[Trait] = BsonDecoder.derived
    val original = BsonDocument(
      "type" -> 5,
      "int" -> 1
    )

    val result = original.fromBson[Trait]
    result should ===(Left(TypeMismatch(BsonType.INT32, BsonType.STRING)))
  }

  it should "fail with an error stack" in {
    case class FooBar(bar: Bar)
    case class Bar(baz: List[Baz])

    sealed trait Baz
    case class Qux(answer: Int) extends Baz

    implicit val bazdecoder: BsonDecoder[Baz] = BsonDecoder.derived
    implicit val bardecoder: BsonDecoder[Bar] = BsonDecoder.derived
    implicit val fooBardecoder: BsonDecoder[FooBar] = BsonDecoder.derived

    val doc = BsonDocument(
      "bar" -> BsonDocument(
        "baz" -> BsonArray.fromIterable(
          List(BsonDocument("type" -> "Qux"), BsonDocument("type" -> "Qux", "answer" -> BsonBoolean(false)))
        )
      )
    )

    val result = doc.fromBson[FooBar]

    result.left.value.stack should ===(ErrorStack(List(Attr("bar"), Attr("baz"), Index(0), Case("Qux"))))
  }

  it should "decode case objects" in {
    case object Foo
    implicit val decoder: BsonDecoder[Foo.type] = BsonDecoder.derived

    val doc = BsonDocument()

    val result = doc.fromBson[Foo.type]

    result.value should ===(Foo)
  }
}
