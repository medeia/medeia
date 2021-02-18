package medeia.enumeratum

import medeia.codec.BsonCodec
import medeia.decoder.BsonDecoder
import medeia.syntax._
import org.scalacheck.{Prop, Properties}

class EnumeratumCodecProperties extends Properties("EnumeratumCodec") with enumeratum.ArbitraryInstances {
  propertyWithSeed("decode after encode === id", None) = {
    implicit val codec: BsonCodec[TestEnum] = Enumeratum.codec(TestEnum)

    Prop.forAll { original: TestEnum =>
      BsonDecoder.decode(original.toBson) == Right(original)
    }
  }
}
