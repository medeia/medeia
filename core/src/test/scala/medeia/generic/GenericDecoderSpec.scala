package medeia.generic

import cats.data.NonEmptyChain
import medeia.MedeiaSpec
import medeia.decoder.BsonDecoderError.{InvalidTypeTag, KeyNotFound, TypeMismatch}
import medeia.decoder.ErrorStack
import medeia.decoder.StackFrame.{Attr, Case, Index}
import medeia.syntax._
import org.bson.BsonType
import org.mongodb.scala.bson.{BsonArray, BsonBoolean, BsonDocument, BsonNull, BsonString}

class GenericDecoderSpec extends MedeiaSpec {

  sealed trait Trait
  case class A(string: String) extends Trait
  case class B(int: Int) extends Trait

  "GenericDecoder" should "handle errors" in {
    import medeia.generic.auto._
    case class Simple(int: Int, string: String)
    val doc = BsonDocument()
    doc.fromBson[Simple] should ===(Left(NonEmptyChain(KeyNotFound("int"), KeyNotFound("string"))))
  }

  it should "decode empty values to None" in {
    import medeia.generic.auto._
    case class Simple(int: Option[Int])
    val doc = BsonDocument()
    doc.fromBson[Simple] should ===(Right(Simple(None)))
  }

  it should "fail gracefully on nested decoders" in {
    import medeia.generic.auto._
    case class Inner(int: Int)
    case class Outer(inner: Inner)
    val doc = BsonDocument(List("inner" -> BsonNull()))
    doc.fromBson[Outer] should ===(Left(NonEmptyChain(TypeMismatch(BsonType.NULL, BsonType.DOCUMENT, ErrorStack(List(Attr("inner")))))))
  }

  it should "allow for key transformation" in {
    import medeia.generic.auto._
    case class Simple(int: Int, string: String)
    object Simple {
      implicit val derivationOptions: GenericDerivationOptions[Simple] = GenericDerivationOptions { case "int" => "intA" }
    }
    val doc = BsonDocument(
      "intA" -> 1,
      "string" -> "string"
    )
    doc.fromBson[Simple] should ===(Right(Simple(1, "string")))
  }

  it should "decode selead trait hierarchies" in {
    import medeia.generic.auto._

    val original = BsonDocument(
      "type" -> "B",
      "int" -> 1
    )

    val result = original.fromBson[Trait]
    result should ===(Right(B(1)))
  }

  //prevents unused field warnings
  object ForSealedTraitWithTransformationTest {
    implicit val coproductDerivationOptions: SealedTraitDerivationOptions[Trait] =
      SealedTraitDerivationOptions(discriminatorTransformation = { case a => a.toLowerCase() }, discriminatorKey = "otherType")
  }

  it should "decode sealed trait hierarchies with transformation" in {
    import medeia.generic.auto._
    import ForSealedTraitWithTransformationTest._
    val original = BsonDocument(
      "otherType" -> "b",
      "int" -> 1
    )

    val result = original.fromBson[Trait]
    result should ===(Right(B(1)))
  }

  it should "fail on unknown discriminator" in {
    import medeia.generic.auto._

    val original = BsonDocument(
      "type" -> "Z",
      "int" -> 1
    )

    val result = original.fromBson[Trait]
    result should ===(Left(NonEmptyChain(InvalidTypeTag("Z"))))
  }

  it should "fail on missing discriminator" in {
    import medeia.generic.auto._

    val original = BsonDocument(
      "int" -> 1
    )

    val result = original.fromBson[Trait]
    result should ===(Left(NonEmptyChain(KeyNotFound("type"))))
  }

  it should "fail on invalid discriminator key" in {
    import medeia.generic.auto._

    val original = BsonDocument(
      "type" -> 5,
      "int" -> 1
    )

    val result = original.fromBson[Trait]
    result should ===(Left(NonEmptyChain(TypeMismatch(BsonType.INT32, BsonType.STRING))))
  }

  it should "fail with an error stack" in {
    import medeia.generic.auto._

    case class FooBar(foo: Foo, bar: Bar)
    case class Foo(i: Int)
    case class Bar(baz: List[Baz])

    sealed trait Baz
    case class Qux(answer: Int) extends Baz

    val doc = BsonDocument(
      "foo" -> BsonDocument("i" -> BsonString("not-an-int")),
      "bar" -> BsonDocument(
        "baz" -> BsonArray.fromIterable(
          List(BsonDocument("type" -> "Qux"), BsonDocument("type" -> "Qux", "answer" -> BsonBoolean(false)))
        )
      )
    )

    val result = doc.fromBson[FooBar]

    result.left.value.map(_.stack).toChain.toList should contain theSameElementsAs List(
      ErrorStack(List(Attr("foo"), Attr("i"))),
      ErrorStack(List(Attr("bar"), Attr("baz"), Index(0), Case("Qux"))),
      ErrorStack(List(Attr("bar"), Attr("baz"), Index(2), Case("Qux"), Attr("answer")))
    )
  }
}
