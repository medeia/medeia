package medeia.generic

import medeia.codec.{BsonCodec, BsonDocumentCodec}
import org.scalacheck.{Gen, Prop, Properties}

class GenericCodecProperties extends Properties("GenericEncoding") {
  propertyWithSeed("decode simple case class", None) = {
    case class Simple(int: Int, string: String)
    val codec: BsonDocumentCodec[Simple] = BsonDocumentCodec.derived

    Prop.forAll { (int: Int, string: String) =>
      val origin = Simple(int, string)
      val encoded = codec.encode(origin)
      val decoded = codec.decode(encoded)
      decoded == Right(origin)
    }
  }

  propertyWithSeed("decode more complex case class", None) = {
    case class Complex(int: Option[Int], stringList: List[String])
    val complexGen = for {
      intOpt <- Gen.option(Gen.posNum[Int])
      stringList <- Gen.listOf(Gen.alphaStr)
    } yield Complex(intOpt, stringList)

    val codec: BsonDocumentCodec[Complex] = BsonDocumentCodec.derived

    Prop.forAll(complexGen) { origin =>
      val encoded = codec.encode(origin)
      val decoded = codec.decode(encoded)
      decoded == Right(origin)
    }
  }
  propertyWithSeed("decode sealed trait hierarchy", None) = {
    sealed trait Trait
    case class A(string: String) extends Trait
    case class B(int: Int) extends Trait
    case object C extends Trait
    val aGen = Gen.alphaStr.map(A.apply)
    val bGen = Gen.posNum[Int].map(B.apply)
    val traitGen = Gen.oneOf[Trait](aGen, bGen, Gen.const(C))

    implicit val codecA: BsonDocumentCodec[A] = BsonDocumentCodec.derived
    implicit val codecB: BsonDocumentCodec[B] = BsonDocumentCodec.derived
    implicit val codecC: BsonDocumentCodec[C.type] = BsonDocumentCodec.derived
    val codec: BsonDocumentCodec[Trait] = BsonDocumentCodec.derived

    Prop.forAll(traitGen) { origin =>
      val encoded = codec.encode(origin)
      val decoded = codec.decode(encoded)
      decoded == Right(origin)
    }
  }

  // prevents unused field warnings
  object ForHierarchyWithOptionsTest {
    sealed trait Trait
    case class A(stringField: String) extends Trait
    case class B(int: Int) extends Trait

    implicit val coproductDerivationOptions: SealedTraitDerivationOptions[Trait] =
      SealedTraitDerivationOptions(discriminatorTransformation = { case a => a.toLowerCase() }, discriminatorKey = "otherType")
    implicit val genericDerivationOptionsA: GenericDerivationOptions[A] = GenericDerivationOptions { case a => a.toLowerCase() }
    implicit val genericDerivationOptionsB: GenericDerivationOptions[B] = GenericDerivationOptions { case a => a.toUpperCase() }
    implicit val codecA: BsonDocumentCodec[A] = BsonDocumentCodec.derived
    implicit val codecB: BsonDocumentCodec[B] = BsonDocumentCodec.derived
    val codec: BsonCodec[Trait] = BsonDocumentCodec.derived
  }

  propertyWithSeed("decode sealed trait hierarchy with Options", None) = {
    import ForHierarchyWithOptionsTest._

    val aGen = Gen.alphaStr.map(A.apply)
    val bGen = Gen.posNum[Int].map(B.apply)
    val traitGen = Gen.oneOf[Trait](aGen, bGen)

    Prop.forAll(traitGen) { origin =>
      val encoded = codec.encode(origin)
      val decoded = codec.decode(encoded)
      decoded == Right(origin)
    }
  }

}
