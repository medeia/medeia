package medeia.generic

import cats.data.NonEmptyChain
import medeia.decoder.BsonDecoderError.{KeyNotFound, TypeMismatch}
import medeia.generic.auto._
import medeia.syntax._
import org.bson.BsonType
import org.mongodb.scala.bson.{BsonDocument, BsonNull}
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{EitherValues, FlatSpec, Matchers}

class GenericDecoderSpec extends FlatSpec with Matchers with EitherValues with TypeCheckedTripleEquals {

  "GenericDecoder" should "handle errors" in {
    case class Simple(int: Int, string: String)
    val doc = BsonDocument()
    doc.fromBson[Simple].left.value should ===(NonEmptyChain(KeyNotFound("int"), KeyNotFound("string")))
  }

  it should "decode empty values to None" in {
    case class Simple(int: Option[Int])
    val doc = BsonDocument()
    doc.fromBson[Simple].right.value should ===(Simple(None))
  }

  it should "fail gracefully on nested decoders" in {
    case class Inner(int: Int)
    case class Outer(inner: Inner)
    val doc = BsonDocument(List("inner" -> BsonNull()))
    doc.fromBson[Outer].left.value should ===(NonEmptyChain(TypeMismatch(BsonType.NULL, BsonType.DOCUMENT)))
  }

  it should "allow for key transformation" in {
    case class Simple(int: Int, string: String)
    val doc = BsonDocument(
      "intA" -> 1,
      "string" -> "string"
    )
    implicit val decoderOptions: GenericDecoderOptions[Simple] = GenericDecoderOptions { case "int" => "intA" }
    doc.fromBson[Simple].right.value should ===(Simple(1, "string"))
  }

}
