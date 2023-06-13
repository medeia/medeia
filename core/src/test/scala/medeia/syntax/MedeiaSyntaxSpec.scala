package medeia.syntax

import cats.data.NonEmptyChain
import medeia.MedeiaSpec
import medeia.codec.{BsonCodec, BsonDocumentCodec}
import medeia.decoder.BsonDecoderError.KeyNotFound
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
  implicit val fooCodec: BsonDocumentCodec[Foo] = BsonDocumentCodec.derived
  implicit val barCodec: BsonDocumentCodec[Bar] = BsonDocumentCodec.derived
  implicit val traitCodec: BsonDocumentCodec[Trait] = BsonDocumentCodec.derived
}
