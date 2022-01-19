package medeia.codec

import medeia.MedeiaSpec
import medeia.decoder.BsonDecoderError.GenericDecoderError
import cats.syntax.either._
import medeia.decoder.BsonDecoderError

class BsonKeyCodecSpec extends MedeiaSpec {
  behavior of "BsonKeyCodec"

  it should "support imap" in {
    val codec = BsonKeyCodec[Int]
    val imappedCodec: BsonKeyCodec[String] = codec.imap(_.toString)(_.toInt)

    val input = "42"

    val result = imappedCodec.decode(imappedCodec.encode(input))

    result should ===(Right(input))
  }

  it should "support iemap" in {
    val codec = BsonKeyCodec[Int]
    val iemappedCodec: BsonKeyCodec[String] = codec.iemap(i => Right(i.toString))(_.toInt)

    val input = "42"

    val result = iemappedCodec.decode(iemappedCodec.encode(input))

    result should ===(Right(input))
  }

  it should "handle failures in iemap" in {
    val codec = BsonKeyCodec[Int]

    val error = "oops"

    val iemappedCodec: BsonKeyCodec[String] = codec.iemap(_ => Left(error))(_.toInt)

    val result = iemappedCodec.decode(iemappedCodec.encode("42"))

    result should ===(Left[BsonDecoderError, String](GenericDecoderError(error)).toEitherNec)
  }
}
