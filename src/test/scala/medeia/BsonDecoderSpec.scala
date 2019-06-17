package medeia

import medeia.decoder.BsonDecoder
import org.mongodb.scala.bson.collection.immutable
import org.mongodb.scala.bson.collection.mutable
import org.mongodb.scala.bson.{BsonDocument, BsonElement, BsonInt32, BsonString}
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._

class BsonDecoderSpec extends FlatSpec with Matchers with TypeCheckedTripleEquals {
  behavior of "BsonDecoder"

  it should "decode BsonValue into BsonDocument" in {
    val doc = new BsonDocument(List(new BsonElement("answer", new BsonInt32(42)), new BsonElement("question", new BsonString("???"))).asJava)

    BsonDecoder[BsonDocument].decode(doc) should be('right)
  }

  it should "decode BsonValue into immutable Document" in {
    val doc = new BsonDocument(List(new BsonElement("answer", new BsonInt32(42)), new BsonElement("question", new BsonString("???"))).asJava)

    BsonDecoder[immutable.Document].decode(doc) should be('right)
  }

  it should "decode BsonValue into mutable Document" in {
    val doc = new BsonDocument(List(new BsonElement("answer", new BsonInt32(42)), new BsonElement("question", new BsonString("???"))).asJava)

    BsonDecoder[mutable.Document].decode(doc) should be('right)
  }
}
