package medeia.syntax

import medeia.decoder.BsonDecoder
import medeia.encoder.BsonDocumentEncoder
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{EitherValues, FlatSpec, Matchers, OptionValues}

class MedeiaSyntaxSpec extends FlatSpec with Matchers with TypeCheckedTripleEquals with EitherValues with OptionValues {
  behavior of classOf[MedeiaSyntax].getSimpleName

  it should "enrich values that have a bson encoder instance" in {
    val input = 42

    val result = input.toBson.fromBson[Int].right.value

    result should ===(input)
  }

  it should "enrich values that have a bson document encoder instance" in {
    case class Foo(answer: Int)
    object Foo {
      implicit val fooEncoder: BsonDocumentEncoder[Foo] = medeia.generic.semiauto.deriveBsonEncoder
      implicit val fooDecoder: BsonDecoder[Foo] = medeia.generic.semiauto.deriveBsonDecoder
    }

    val input = Foo(42)

    val result = input.toBson.fromBson[Foo].right.value

    result should ===(input)
  }
}
