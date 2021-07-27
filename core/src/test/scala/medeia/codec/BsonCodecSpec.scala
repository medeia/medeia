package medeia.codec

import cats.syntax.either._
import medeia.MedeiaSpec
import medeia.decoder.BsonDecoderError.GenericDecoderError

class BsonCodecSpec extends MedeiaSpec {
  behavior of "BsonCodec"

  it should "support imap" in {
    val codec = BsonCodec[Int]
    val imappedCodec: BsonCodec[String] = codec.imap(_.toString)(_.toInt)

    val input = "42"

    val result = imappedCodec.decode(imappedCodec.encode(input))

    result should ===(Right(input))
  }

  it should "support iemap" in {
    val codec = BsonCodec[Int]
    val iemappedCodec: BsonCodec[String] = codec.iemap(i => Right(i.toString))(_.toInt)

    val input = "42"

    val result = iemappedCodec.decode(iemappedCodec.encode(input))

    result should ===(Right(input))
  }

  it should "handle failures in iemap" in {
    val codec = BsonCodec[Int]

    val error = "oops"

    val iemappedCodec: BsonCodec[String] = codec.iemap(_ => Left(error))(_.toInt)

    val result = iemappedCodec.decode(iemappedCodec.encode("42"))

    result should ===(Left(GenericDecoderError(error)).toEitherNec)
  }
}
