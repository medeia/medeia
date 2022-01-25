package medeia.refined

import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.Negative
import eu.timepit.refined.scalacheck.collection._
import eu.timepit.refined.scalacheck.numeric._
import eu.timepit.refined.scalacheck.string._
import eu.timepit.refined.string.Uuid
import medeia.codec.BsonCodec
import medeia.decoder.BsonDecoder
import medeia.syntax._
import org.scalacheck.{Arbitrary, Prop, Properties}

class RefinedCodecProperties extends Properties("RefinedCodec") {

  propertyWithSeed("decode after encode === id (string)", None) = {
    codecProperty[Refined[String, Uuid]]
  }

  propertyWithSeed("decode after encode === id (list)", None) = {
    codecProperty[Refined[List[String], NonEmpty]]
  }

  propertyWithSeed("decode after encode === id (int)", None) = {
    codecProperty[Refined[Int, Negative]]
  }

  private[this] def codecProperty[A: Arbitrary: BsonCodec]: Prop =
    Prop.forAll { (original: A) =>
      BsonDecoder.decode[A](original.toBson) == Right(original)
    }
}
