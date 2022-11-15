# medeia
[![Maven Central](https://img.shields.io/maven-central/v/de.megaera/medeia_2.13.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22de.megaera%22)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)
<a href="https://typelevel.org/cats/"><img src="https://typelevel.org/cats/img/cats-badge.svg" height="40px" align="right" alt="Cats friendly" /></a>

medeia is a BSON library for Scala.

## Goal / Scope

The goal of medeia is to make bson decoding / encoding as easy and fun as JSON decoding / encoding is with circe or argonaut.

medeia includes decoders and encoders for common data structures as well as automatic derivation of decoders and encoders for case classes using shapeless.

## Quickstart

### Add to sbt

```sbt
  libraryDependencies += "de.megaera" %% "medeia" % "0.8.2"
```

Currently no complete documentation is present, additional examples can be found in the test suites.
If you have questions: don't hesitate to ask via github issues.

### decoding and encoding example:

```scala
  import org.mongodb.scala.bson._

  import medeia.syntax._

  val stringList = List("a", "b")
  val encoded = stringList.toBson
  // BsonArray{values=[BsonString{value='a'}, BsonString{value='b'}]}
  val bsonArray = BsonArray("a", "b")
  val decoded = bsonArray.fromBson[List[String]]
  // Right(List(a,b)
```

### automatic derivation example for case classes and sealed trait hierarchies:

```scala
  import org.mongodb.scala.bson._

  import medeia.encoder.BsonEncoder
  import medeia.decoder.BsonDecoder
  import medeia.codec._
  import medeia.syntax._

  case class Simple(int: Int, string: Option[String])
  implicit val simpleEncoder: BsonEncoder[Simple] = BsonEncoder.derive
  val encoded = Simple(1, Some("a")).toBson
  // {"string": "a", "int": 1}

  implicit val simpleDecoder: BsonDecoder[Simple] = BsonDecoder.derive
  val doc = BsonDocument("int" -> 1, "string" -> "string")
  val decoded = doc.fromBson[Simple]
  // Right(Simple(1,Some(string)))

  sealed trait Trait
  case class Foo(answer: Int) extends Trait
  case class Bar(bar: String) extends Trait

  implicit val fooCodec: BsonDocumentCodec[Foo] = BsonCodec.derive
  implicit val barCodec: BsonDocumentCodec[Bar] = BsonCodec.derive
  implicit val traitCodec: BsonDocumentCodec[Trait] = BsonCodec.derive

  val encoded = Foo(42).toBson
  // {"answer": 42, "type": "Foo"}
```

A transformation function for keynames can be provided as follows:

```scala
  import medeia.generic.GenericDerivationOptions
  import medeia.encoder.BsonEncoder
  import medeia.syntax._

  case class Simple(fieldInScala: Int)
  implicit val genericDerivationOptions: GenericDerivationOptions[Simple] =
    GenericDerivationOptions { case "fieldInScala" => "fieldInBson" }
  implicit val simpleEncoder: BsonEncoder[Simple] = BsonEncoder.derive
  val encoded = Simple(1).toBson
  // {"fieldInBson": 1}
```

GenericDerivationOptions works for encoding and decoding.
If the provided partial function is not defined for a key no transformation is used.

### Enumeratum

A separate module exists for encoding and decoding [enumeratum](https://github.com/lloydmeta/enumeratum) enums:

#### Add sbt dependency

```sbt
  libraryDependencies += "de.megaera" %% "medeia-enumeratum" % "0.8.2"
```

#### Usage

```scala
  import medeia.syntax._
  import medeia.enumeratum.Enumeratum

  import enumeratum.Enum
  import enumeratum.EnumEntry

  import scala.collection.immutable

  sealed abstract class TestEnum(override val entryName: String) extends EnumEntry

  object TestEnum extends Enum[TestEnum] {
    override val values: immutable.IndexedSeq[TestEnum] = findValues

    case object A extends TestEnum("A")
    case object B extends TestEnum("B")
    case object C extends TestEnum("C")

    implicit val codec: BsonCodec[TestEnum] = Enumeratum.codec(TestEnum)
  }

  TestEnum.A.toBson
  // "A"
```

### Refined

BsonEncoder/BsonDecoder for `eu.timepit:refined` can be found in the `medeia-refined` module

#### Add sbt dependency

```sbt
  libraryDependencies += "de.megaera" %% "medeia-refined" % "0.8.2"
```

#### Usage

```scala
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.collection._
import medeia.syntax._
import medeia.refined._

val refinedString: String Refined NonEmpty = "test"
refinedString.toBson
// BsonString{value='test'}
```

## License

medeia is licensed under the Apache License, Version 2.0 (the “License”); you may not use this software except in compliance with the License.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
