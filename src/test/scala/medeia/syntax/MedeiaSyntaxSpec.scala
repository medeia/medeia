package medeia.syntax

import cats.data.NonEmptyChain
import medeia.MedeiaSpec
import medeia.decoder.BsonDecoder
import medeia.decoder.BsonDecoderError.KeyNotFound
import medeia.encoder.BsonDocumentEncoder
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.bson.{BsonDocument, BsonString}

class MedeiaSyntaxSpec extends MedeiaSpec {
  behavior of classOf[MedeiaSyntax].getSimpleName

  it should "enrich values that have a bson encoder instance" in {
    val input = 42

    val result = input.toBson.fromBson[Int]

    result should ===(Right(input))
  }

  it should "enrich values that have a bson document encoder instance" in {
    val input: Trait = Foo(42)

    try { input.toBson } catch {
      case e: Throwable => e.printStackTrace()
    }

    val result = input.toBson.fromBson[Trait]

    result should ===(Right(input))
  }

  it should "support getSafe for Document" in {
    Document("existing" -> "foo").getSafe("existing") should ===(Right(BsonString("foo")))
    Document().getSafe("nonexisting") should ===(Left(NonEmptyChain(KeyNotFound("nonexisting"))))
  }

  it should "support getSafe for BsonDocument" in {
    BsonDocument("existing" -> "foo").getSafe("existing") should ===(Right(BsonString("foo")))
    BsonDocument().getSafe("nonexisting") should ===(Left(NonEmptyChain(KeyNotFound("nonexisting"))))
  }
}
sealed trait Trait
case class Foo(answer: Int) extends Trait
case class Bar(bar: String) extends Trait
object Trait {
  implicit val fooEncoder: BsonDocumentEncoder[Foo] = medeia.generic.semiauto.deriveBsonEncoder
  implicit val fooDecoder: BsonDecoder[Foo] = medeia.generic.semiauto.deriveBsonDecoder

  implicit val barEncoder: BsonDocumentEncoder[Bar] = medeia.generic.semiauto.deriveBsonEncoder
  implicit val barDecoder: BsonDecoder[Bar] = medeia.generic.semiauto.deriveBsonDecoder

  implicit val traitEncoder: BsonDocumentEncoder[Trait] = medeia.generic.semiauto.deriveBsonEncoder[Trait]
  implicit val traitDecoder: BsonDecoder[Trait] = medeia.generic.semiauto.deriveBsonDecoder[Trait]
}
