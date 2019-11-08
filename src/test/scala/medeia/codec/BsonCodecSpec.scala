package medeia.codec

import medeia.MedeiaSpec

class BsonCodecSpec extends MedeiaSpec {
  behavior of "BsonCodec"

  it should "support imap" in {
    val codec = BsonCodec[Int]
    val imappedCodec: BsonCodec[String] = codec.imap(_.toString)(_.toInt)

    val input = "42"

    val result = imappedCodec.decode(imappedCodec.encode(input))

    result should ===(Right(input))
  }
}
