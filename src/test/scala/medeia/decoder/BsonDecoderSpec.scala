package medeia.decoder

import medeia.MedeiaSpec
import org.bson.BsonValue
import org.mongodb.scala.bson.collection.{immutable, mutable}
import org.mongodb.scala.bson.{BsonDocument, BsonElement, BsonInt32, BsonString}

import scala.jdk.CollectionConverters._

class BsonDecoderSpec extends MedeiaSpec {
  behavior of "BsonDecoder"

  it should "decode BsonValue into BsonDocument" in {
    val doc = new BsonDocument(List(new BsonElement("answer", new BsonInt32(42)), new BsonElement("question", new BsonString("???"))).asJava)

    BsonDecoder[BsonDocument].decode(doc) should be(Symbol("right"))
  }

  it should "decode BsonValue into immutable Document" in {
    val doc = new BsonDocument(List(new BsonElement("answer", new BsonInt32(42)), new BsonElement("question", new BsonString("???"))).asJava)

    BsonDecoder[immutable.Document].decode(doc) should be(Symbol("right"))
  }

  it should "decode BsonValue into mutable Document" in {
    val doc = new BsonDocument(List(new BsonElement("answer", new BsonInt32(42)), new BsonElement("question", new BsonString("???"))).asJava)

    BsonDecoder[mutable.Document].decode(doc) should be(Symbol("right"))
  }

  it should "decode BsonValue into BsonValue" in {
    val bsonValue: BsonValue = new BsonString("")

    BsonDecoder[BsonValue].decode(bsonValue) should ===(Right(bsonValue))
  }
}
