package medeia.enumeratum

import medeia.codec.BsonCodec
import medeia.decoder.BsonDecoder
import medeia.syntax._
import org.scalacheck.{Prop, Properties}

class EnumeratumCodecProperties extends Properties("EnumeratumCodec") with enumeratum.ArbitraryInstances with enumeratum.values.ArbitraryInstances {
  propertyWithSeed("Enum: decode after encode === id", None) = {
    implicit val codec: BsonCodec[TestEnum] = Enumeratum.codec(TestEnum)

    Prop.forAll { (original: TestEnum) =>
      BsonDecoder.decode(original.toBson) == Right(original)
    }
  }

  propertyWithSeed("IntEnum decode after encode === id", None) = {
    implicit val codec: BsonCodec[TestIntEnum] = Enumeratum.valueEnumCodec(TestIntEnum)

    Prop.forAll { (original: TestIntEnum) =>
      BsonDecoder.decode(original.toBson) == Right(original)
    }
  }
}
