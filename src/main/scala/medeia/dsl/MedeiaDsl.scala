package medeia.dsl

import scala.collection.JavaConverters._
import cats.data.EitherNec
import cats.instances.parallel._
import cats.syntax.either._
import cats.syntax.parallel._
import medeia.decoder.{BsonDecoder, BsonDecoderError}
import org.bson.{BsonType, BsonValue}
import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonElement, BsonInt32, BsonString}

case class FooInDB(name: String, things1: List[Int], things2: List[Double])
case class Foo(name: String, things: List[(Int, Double)])

object Foo {
  import MedeiaDsl._

  implicit val fooBsonDecoder: BsonDecoder[Foo] = withDocument { doc =>
    (doc.get[String]("name"), doc.get[List[Int]]("things1"), doc.get[List[Double]]("things2")).parMapN {
      case (name, things1, things2) => Foo(name, things1.zip(things2))
    }
  }
}

case class MedeiaDocument(bsonDocument: BsonDocument) {
  def get[A](key: String)(implicit decoder: BsonDecoder[A]): EitherNec[BsonDecoderError, A] = {
    val result = bsonDocument.get(key)

    if (result == null) {
      BsonDecoderError.KeyNotFound(key).leftNec
    } else {
      decoder.decode(result)
    }
  }
}

object MedeiaDsl {

  def withDocument[A](f: MedeiaDocument => EitherNec[BsonDecoderError, A])(bson: BsonValue): EitherNec[BsonDecoderError, A] = {
    bson.getBsonType match {
      case BsonType.DOCUMENT => f(MedeiaDocument(bson.asDocument()))
      case _                 => BsonDecoderError.TypeMismatch(bson.getBsonType, BsonType.DOCUMENT).leftNec
    }
  }

}

object Main extends App {
  private val failing1: EitherNec[BsonDecoderError, Foo] = BsonDecoder[Foo].decode(new BsonInt32(42))
  println(failing1) // Left(Chain(expected: DOCUMENT, actual: INT32))

  private val failing2: EitherNec[BsonDecoderError, Foo] =
    BsonDecoder[Foo].decode(
      new BsonDocument(List(new BsonElement("name", new BsonString("foo name")), new BsonElement("wrongname", new BsonArray())).asJava))

  println(failing2) // Left(Chain(Key not found: things1, Key not found: things2))

  private val success: EitherNec[BsonDecoderError, Foo] =
    BsonDecoder[Foo].decode(
      new BsonDocument(
        List(new BsonElement("name", new BsonString("foo name")),
             new BsonElement("things1", new BsonArray()),
             new BsonElement("things2", new BsonArray())).asJava))

  println(success) // Right(Foo(foo name,List()))

}
