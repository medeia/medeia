package medeia.generic

import medeia.codec.BsonCodec
import org.scalacheck.{Gen, Prop, Properties}

class GenericCodecProperties extends Properties("GenericEncoding") {
  import medeia.generic.GenericDecoder._
  import medeia.generic.GenericEncoder._

  propertyWithSeed("decode simple case class", None) = {
    case class Simple(int: Int, string: String)
    val codec: BsonCodec[Simple] = BsonCodec.fromEncoderAndDecoder

    Prop.forAll { (int: Int, string: String) =>
      val origin = Simple(int, string)
      val encoded = codec.encode(origin)
      val decoded = codec.decode(encoded)
      decoded == Right(origin)
    }
  }

  propertyWithSeed("decode more complex case class", None) = {
    case class Complex(int: Option[Int], stringList: List[String])
    val complexGen = for {
      intOpt <- Gen.option(Gen.posNum[Int])
      stringList <- Gen.listOf(Gen.alphaStr)
    } yield Complex(intOpt, stringList)
    val codec: BsonCodec[Complex] = BsonCodec.fromEncoderAndDecoder

    Prop.forAll(complexGen) { origin =>
      val encoded = codec.encode(origin)
      val decoded = codec.decode(encoded)
      decoded == Right(origin)
    }
  }

}
