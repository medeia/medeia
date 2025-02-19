package medeia.generic

import medeia.MedeiaSpec
import medeia.codec.BsonDocumentCodec
import medeia.decoder.BsonDecoder
import medeia.encoder.BsonDocumentEncoder

class SemiautoSpec extends MedeiaSpec {
  case class Simple(int: String)
  sealed trait Trait
  case class A(string: String) extends Trait
  case object B extends Trait

  "Semiauto" should "be able to derive an encoder from a case class" in {
    BsonDocumentEncoder.derived[Simple]
  }

  it should "be able to derive a decoder from a case class" in {
    val _: BsonDecoder[Simple] = BsonDecoder.derived[Simple]
  }

  it should "be able to derive a codec from a case class" in {
    val _: BsonDocumentCodec[Simple] = BsonDocumentCodec.derived[Simple]
  }

  "The default summoning method" should "not be able to access GenericEncoders without import" in {
    "medeia.encoder.BsonEncoder[Simple]" shouldNot typeCheck
  }

  it should "not be able to access GenericDecoders without import" in {
    "medeia.decoder.BsonDecoder[Simple]" shouldNot typeCheck
  }

  it should "not be able to access GenericCodecs without import" in {
    "medeia.codec.BsonCodec[Simple]" shouldNot typeCheck
  }
}
