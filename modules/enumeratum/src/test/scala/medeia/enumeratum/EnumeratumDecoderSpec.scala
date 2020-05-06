package medeia.enumeratum

import medeia.decoder.BsonDecoderError.FieldParseError
import cats.data.NonEmptyChain
import org.mongodb.scala.bson.BsonString
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class EnumeratumDecoderSpec extends AnyFlatSpecLike with TypeCheckedTripleEquals with Matchers {
  behavior of "EnumeratumBsonDecoder"

  it should "fail gracefully on unknown entry" in {
    val unknown = "unknown"

    Enumeratum.decoder(TestEnum).decode(BsonString(unknown)) should
      ===(Left(NonEmptyChain(FieldParseError(s"Unable to find enum with name $unknown, valid choices include ${TestEnum.values.take(3)}"))))
  }
}
