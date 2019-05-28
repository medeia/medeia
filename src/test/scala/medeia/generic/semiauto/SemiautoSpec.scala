package medeia.generic.semiauto

import org.scalatest.{FlatSpec, Matchers}

class SemiautoSpec extends FlatSpec with Matchers {
  case class Simple(int: Int)

  "Semiauto.deriveEncoder" should "be able to derive an encoder from a case class" in {
    "deriveEncoder[Simple]" should compile
  }

  "The default summoning method" should "not be able to access GenericEncoders without import" in {
    "BsonEncoder[Simple]" shouldNot typeCheck
  }
}
