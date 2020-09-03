package medeia.codec

import java.time.Instant
import java.util.{Date, UUID}

import cats.data.{Chain, NonEmptyChain, NonEmptyList, NonEmptySet}
import medeia.Arbitraries
import medeia.decoder.BsonDecoder
import medeia.syntax._
import org.scalacheck.{Arbitrary, Prop, Properties}

import scala.collection.immutable.SortedSet

class BsonCodecProperties extends Properties("BsonCodec") with Arbitraries {
  propertyWithSeed("decode after encode === id (boolean)", None) = {
    codecProperty[Boolean]
  }

  propertyWithSeed("decode after encode === id (string)", None) = {
    codecProperty[String]
  }

  propertyWithSeed("decode after encode === id (long)", None) = {
    codecProperty[Long]
  }

  propertyWithSeed("decode after encode === id (double)", None) = {
    codecProperty[Double]
  }

  propertyWithSeed("decode after encode === id (instant)", None) = {
    codecProperty[Instant]
  }

  propertyWithSeed("decode after encode === id (date)", None) = {
    codecProperty[Date]
  }

  propertyWithSeed("decode after encode === id (binary)", None) = {
    codecProperty[Array[Byte]]
  }

  propertyWithSeed("decode after encode === id (symbol)", None) = {
    codecProperty[Symbol]
  }

  propertyWithSeed("decode after encode === id (option)", None) = {
    codecProperty[Option[String]]
  }

  propertyWithSeed("decode after encode === id (list)", None) = {
    codecProperty[List[String]]
  }

  propertyWithSeed("decode after encode === id (set)", None) = {
    codecProperty[Set[String]]
  }

  propertyWithSeed("decode after encode === id (sortedset)", None) = {
    codecProperty[SortedSet[String]]
  }

  propertyWithSeed("decode after encode === id (vector)", None) = {
    codecProperty[Vector[String]]
  }

  propertyWithSeed("decode after encode === id (chain)", None) = {
    codecProperty[Chain[String]]
  }

  propertyWithSeed("decode after encode === id (map)", None) = {
    codecProperty[Map[UUID, String]]
  }

  propertyWithSeed("decode after encode === id (nonemptylist)", None) = {
    codecProperty[NonEmptyList[String]]
  }

  propertyWithSeed("decode after encode === id (nonemptychain)", None) = {
    codecProperty[NonEmptyChain[String]]
  }

  propertyWithSeed("decode after encode === id (nonemptyset)", None) = {
    codecProperty[NonEmptySet[String]]
  }

  propertyWithSeed("decode after encode === id (uuid)", None) = {
    codecProperty[UUID]
  }

  private[this] def codecProperty[A: Arbitrary: BsonCodec]: Prop =
    Prop.forAll { original: A =>
      BsonDecoder.decode[A](original.toBson) == Right(original)
    }
}
