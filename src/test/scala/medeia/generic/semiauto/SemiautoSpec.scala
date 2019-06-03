package medeia.generic.semiauto

import org.scalatest.{FlatSpec, Matchers}

class SemiautoSpec extends FlatSpec with Matchers {
  case class Simple(int: String)

  "Semiauto" should "be able to derive an encoder from a case class" in {
    deriveEncoder[Simple]
  }

  it should "be able to derive a decoder from a case class" in {
    val _: medeia.decoder.BsonDecoder[Simple] = deriveDecoder
  }

  it should "be able to derive a codec from a case class" in {
    val _: medeia.BsonCodec[Simple] = deriveCodec
  }

  "The default summoning method" should "not be able to access GenericEncoders without import" in {
    "medeia.encoder.BsonEncoder[Simple]" shouldNot typeCheck
  }

  it should "not be able to access GenericDecoders without import" in {
    "medeia.decoder.BsonDecoder[Simple]" shouldNot typeCheck
  }

  it should "not be able to access GenericCodecs without import" in {
    "medeia.BsonCodec[Simple]" shouldNot typeCheck
  }
}
