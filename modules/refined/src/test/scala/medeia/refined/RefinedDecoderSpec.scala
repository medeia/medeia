package medeia.refined

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Uuid
import medeia.decoder.BsonDecoder
import medeia.decoder.BsonDecoderError.GenericDecoderError
import org.mongodb.scala.bson.BsonString
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class RefinedDecoderSpec extends AnyFlatSpecLike with Matchers with EitherValues {
  behavior of "RefinedBsonDecoder"

  it should "fail gracefully on failed refinement" in {
    val incorrectString = BsonString("No UUID at all")
    BsonDecoder[Refined[String, Uuid]].decode(incorrectString).left.value shouldBe a[GenericDecoderError]
  }
}
