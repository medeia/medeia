package medeia.codec

import medeia.MedeiaSpec

class BsonDocumentCodecSpec extends MedeiaSpec {
  behavior of "BsonDocumentCodec"

  it should "support imap" in {
    import medeia.generic.semiauto._

    case class Foo(answer: Int)
    implicit val codec: BsonDocumentCodec[Foo] = deriveBsonCodec[Foo]

    val imappedCodec: BsonDocumentCodec[Int] = codec.imap(_.answer)(Foo)

    val input = 42

    val result = imappedCodec.decode(imappedCodec.encode(input)).right.value

    result should ===(input)
  }
}
