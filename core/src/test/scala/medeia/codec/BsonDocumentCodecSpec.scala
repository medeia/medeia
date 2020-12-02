package medeia.codec

import medeia.MedeiaSpec

class BsonDocumentCodecSpec extends MedeiaSpec {
  behavior of "BsonDocumentCodec"

  it should "support imap" in {

    case class Foo(answer: Int)
    implicit val codec: BsonDocumentCodec[Foo] = BsonCodec.derive[Foo]

    val imappedCodec: BsonDocumentCodec[Int] = codec.imap(_.answer)(Foo)

    val input = 42

    val result = imappedCodec.decode(imappedCodec.encode(input))

    result should ===(Right(input))
  }
}
