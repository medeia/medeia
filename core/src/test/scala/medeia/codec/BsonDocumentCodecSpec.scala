package medeia.codec

import cats.syntax.either._
import medeia.MedeiaSpec
import medeia.decoder.BsonDecoderError.GenericDecoderError

class BsonDocumentCodecSpec extends MedeiaSpec {
  behavior of "BsonDocumentCodec"

  case class Foo(answer: Int)
  implicit val derivedCodec: BsonDocumentCodec[Foo] = BsonCodec.derive[Foo]

  it should "support imap" in {
    val imappedCodec: BsonDocumentCodec[Int] = derivedCodec.imap(_.answer)(Foo)

    val input = 42

    val result = imappedCodec.decode(imappedCodec.encode(input))

    result should ===(Right(input))
  }

  it should "support iemap" in {
    val codec = BsonDocumentCodec[Foo]
    val iemappedCodec: BsonDocumentCodec[Int] = codec.iemap(i => Right(i.answer))(Foo)

    val input = 42

    val result = iemappedCodec.decode(iemappedCodec.encode(input))

    result should ===(Right(input))
  }

  it should "handle failures in iemap" in {
    val codec = BsonDocumentCodec[Foo]

    val error = "oops"

    val iemappedCodec: BsonDocumentCodec[Int] = codec.iemap(_ => Left(error))(Foo)

    val result = iemappedCodec.decode(iemappedCodec.encode(42))

    result should ===(Left(GenericDecoderError(error)).toEitherNec)
  }
}
