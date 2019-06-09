# medeia
[![Build status](https://img.shields.io/travis/medeia/medeia/master.svg)](https://travis-ci.com/medeia/medeia)

medeia is a BSON library for Scala.

## Goal / Motivation

The goal of medeia is to make bson decoding / encoding as easy and fun as JSON decoding / encoding is with circe or argonaut.

## Quickstart

medeia includes decoders and encoders for common data structures as well as automatic derivation of decoders and encoders for case classes using shapeless.

### Add to sbt

```sbt
libraryDependencies += "de.megaera" % "medeia_2.12" % "0.1.2"
```

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

### automatic derivation example:

```scala
  import org.mongodb.scala.bson._

  import medeia.encoder.BsonEncoder
  import medeia.decoder.BsonDecoder
  import medeia.generic.semiauto._
  import medeia.syntax._

  case class Simple(int: Int, string: Option[String])
  implicit val simpleEncoder: BsonEncoder[Simple] = deriveEncoder
  val encoded = Simple(1, Some("a")).toBson
  // {"string": "a", "int": 1}

  implicit val simpleDecoder: BsonDecoder[Simple] = deriveDecoder
  val doc = BsonDocument("int" -> 1, "string" -> "string")
  val decoded = doc.fromBson[Simple]
  // Right(Simple(1,Some(string)))
```

A transformation function for keynames can be provided as follows:

```scala
  import medeia.generic.GenericDerivationOptions
  import medeia.encoder.BsonEncoder
  import medeia.generic.semiauto._
  import medeia.syntax._

  case class Simple(fieldInScala: Int)
  implicit val genericDerivationOptions: GenericDerivationOptions[Simple] =
    GenericDerivationOptions { case "fieldInScala" => "fieldInBson" }
  implicit val simpleEncoder: BsonEncoder[Simple] = deriveEncoder
  val encoded = Simple(1).toBson
  // {"fieldInBson": 1}
```

GenericDerivationOptions works for encoding and decoding.
If the provided partial function does not is not defined for a key no tranformation is used.

## License

medeia is licensed under the Apache License, Version 2.0 (the “License”); you may not use this software except in compliance with the License.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.