package medeia.codec

import medeia.MedeiaSpec

class BsonKeyCodecSpec extends MedeiaSpec {
  behavior of "BsonKeyCodec"

  it should "support imap" in {
    val codec = BsonKeyCodec[Int]
    val imappedCodec: BsonKeyCodec[String] = codec.imap(_.toString)(_.toInt)

    val input = "42"

    val result = imappedCodec.decode(imappedCodec.encode(input)).right.value

    result should ===(input)
  }
}
