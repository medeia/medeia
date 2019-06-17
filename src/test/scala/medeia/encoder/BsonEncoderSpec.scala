package medeia.encoder

import medeia.BsonCodec
import org.mongodb.scala.bson.{BsonInt32, BsonString, BsonValue}
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{FlatSpec, Matchers}

class BsonEncoderSpec extends FlatSpec with Matchers with TypeCheckedTripleEquals {
  behavior of "BsonEncoder"

  it should "encode BsonValue" in {
    val input = new BsonString("")

    val encoded = BsonEncoder[BsonValue].encode(input)

    encoded should ===(input)
  }

  it should "encode case class with BsonValues" in {
    import medeia.generic.semiauto._
    import medeia.syntax._

    case class Foo(bsonString: BsonString, bsonInt32: BsonInt32)

    val input = Foo(new BsonString("string"), new BsonInt32(42))

    implicit val fooCodec: BsonCodec[Foo] = deriveCodec[Foo]

    val result = input.toBson.fromBson[Foo]

    result should ===(Right(input))
  }
}
