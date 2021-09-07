package medeia.enumeratum

import medeia.decoder.BsonDecoderError.FieldParseError
import org.mongodb.scala.bson.{BsonInt32, BsonString}
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class EnumeratumDecoderSpec extends AnyFlatSpecLike with TypeCheckedTripleEquals with Matchers with EitherValues {
  behavior of "EnumeratumBsonDecoder"

  it should "Enum: fail gracefully on unknown entry" in {
    val unknown = "unknown"

    Enumeratum.decoder(TestEnum).decode(BsonString(unknown)).left.value.head shouldBe a[FieldParseError]
  }

  it should "IntEnum: fail gracefully on unknown entry" in {
    val unknown = 42

    Enumeratum.valueEnumDecoder(TestIntEnum).decode(BsonInt32(unknown)).left.value.head shouldBe a[FieldParseError]
  }
}
