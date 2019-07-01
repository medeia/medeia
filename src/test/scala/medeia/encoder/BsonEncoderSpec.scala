package medeia.encoder

import medeia.{BsonCodec, MedeiaSpec}
import org.mongodb.scala.bson.{BsonInt32, BsonString, BsonValue}

class BsonEncoderSpec extends MedeiaSpec {
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

    implicit val fooCodec: BsonCodec[Foo] = deriveBsonCodec[Foo]

    val result = input.toBson.fromBson[Foo]

    result should ===(Right(input))
  }
}
