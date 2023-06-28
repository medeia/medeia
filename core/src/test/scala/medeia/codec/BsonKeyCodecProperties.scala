package medeia.codec

import java.util.UUID

import medeia.Arbitraries
import medeia.decoder.BsonKeyDecoder
import medeia.encoder.BsonKeyEncoder
import org.scalacheck.{Arbitrary, Prop, Properties}
import java.util.Locale

class BsonKeyCodecProperties extends Properties("BsonKeyCodec") with Arbitraries {
  propertyWithSeed("decode after encode === id (string)", None) = {
    codecProperty[String]
  }

  propertyWithSeed("decode after encode === id (int)", None) = {
    codecProperty[Int]
  }

  propertyWithSeed("decode after encode === id (long)", None) = {
    codecProperty[Long]
  }

  propertyWithSeed("decode after encode === id (double)", None) = {
    codecProperty[Double]
  }

  propertyWithSeed("decode after encode === id (uuid)", None) = {
    codecProperty[UUID]
  }

  propertyWithSeed("decode after encode === id (locale)", None) = {
    codecProperty[Locale]
  }

  private[this] def codecProperty[A: Arbitrary: BsonKeyCodec]: Prop =
    Prop.forAll { (original: A) =>
      BsonKeyDecoder.decode[A](BsonKeyEncoder.encode[A](original)) == Right(original)
    }
}
