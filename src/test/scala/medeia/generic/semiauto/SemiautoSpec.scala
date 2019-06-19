package medeia.generic.semiauto

import medeia.MedeiaSpec
import medeia.codec.BsonDocumentCodec

class SemiautoSpec extends MedeiaSpec {
  case class Simple(int: String)

  "Semiauto" should "be able to derive an encoder from a case class" in {
    deriveBsonEncoder[Simple]
  }

  it should "be able to derive a decoder from a case class" in {
    val _: medeia.decoder.BsonDecoder[Simple] = deriveBsonDecoder
  }

  it should "be able to derive a codec from a case class" in {
    val _: BsonDocumentCodec[Simple] = deriveBsonCodec
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
