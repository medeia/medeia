package medeia.decoder

import cats.data.NonEmptyChain
import medeia.MedeiaSpec
import medeia.decoder.StackFrame.{Index, MapKey}
import org.bson.BsonValue
import org.mongodb.scala.bson.collection.{immutable, mutable}
import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonElement, BsonInt32, BsonString}

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

  it should "provide field info when failing to decode a map" in {
    val key = "foo"
    val doc = new BsonDocument(key, BsonString(""))

    val result = BsonDecoder[Map[String, Int]].decode(doc)

    result.left.value.head.stack should ===(ErrorStack(List(MapKey(key))))
  }

  it should "provide index info when failing to decode an iterable" in {
    val bsonValue: BsonValue = new BsonArray(List(new BsonString("valid"), new BsonInt32(42), new BsonString("valid"), new BsonInt32(1337)).asJava)

    val result = BsonDecoder[List[String]].decode(bsonValue)

    result.left.value.map(_.stack) should ===(NonEmptyChain.of(ErrorStack(List(Index(1))), ErrorStack(List(Index(3)))))
  }
}
