package medeia

import medeia.codec.*
import medeia.encoder.*
import medeia.decoder.*
import medeia.generic.*
import medeia.syntax.*

class Scala3DerivesSpec extends MedeiaSpec {
  behavior of "Scala 3 derives syntax"

  it should "be able to derive case class Codec" in {
    case class Foo(a: String, b: Int) derives BsonDocumentCodec
    val input = Foo("a", 1)
    val result = input.toBson.fromBson[Foo]

    result should ===(Right(input))
  }

  it should "be able to derive case class Encoder and Decoder" in {
    case class Foo(a: String, b: Int) derives BsonDocumentCodec
    val input = Foo("a", 1)
    val result = input.toBson.fromBson[Foo]

    result should ===(Right(input))
  }

  sealed trait Trait derives BsonDocumentCodec
  case class A(stringField: String) extends Trait
  case class B(int: Int) extends Trait

  object Trait {
    implicit val coproductDerivationOptions: GenericDerivationOptions[Trait] =
      GenericDerivationOptions(discriminatorTransformation = { case a => a.toLowerCase() }, discriminatorKey = "otherType")
  }
  object A {
    implicit val genericDerivationOptionsA: GenericDerivationOptions[A] = GenericDerivationOptions { case a => a.toLowerCase() }
  }
  object B {
    implicit val genericDerivationOptionsB: GenericDerivationOptions[B] = GenericDerivationOptions { case a => a.toUpperCase() }
  }

  it should "be able to derive sealed trait Codec" in {

    val input: Trait = A("acontent")
    val result = input.toBson.fromBson[Trait]
    val typefield = input.toBsonDocument.getSafe("otherType").map(_.asString.getValue)
    val stringField = input.toBsonDocument.getSafe("stringfield").map(_.asString.getValue)

    typefield should ===(Right("a"))
    stringField should ===(Right("acontent"))

    result should ===(Right(input))
  }

  enum TestEnum derives BsonDocumentCodec {
    case A1(stringField: String)
    case B2(int: Int)
  }

  object TestEnum {
    given GenericDerivationOptions[TestEnum] =
      GenericDerivationOptions(discriminatorTransformation = { case a => a.toLowerCase() }, discriminatorKey = "otherType")
    given GenericDerivationOptions[A1] = GenericDerivationOptions { case a => a.toLowerCase() }
    given GenericDerivationOptions[B2] = GenericDerivationOptions { case a => a.toUpperCase() }
  }

  it should "be able to derive Enum Codec" in {

    val input: TestEnum = TestEnum.A1("acontent")
    val result = input.toBson.fromBson[TestEnum]
    val typefield = input.toBsonDocument.getSafe("otherType").map(_.asString.getValue)
    val stringField = input.toBsonDocument.getSafe("stringfield").map(_.asString.getValue)

    stringField should ===(Right("acontent"))
    typefield should ===(Right("a1"))
    result should ===(Right(input))
  }

}
