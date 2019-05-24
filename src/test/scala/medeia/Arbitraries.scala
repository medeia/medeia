package medeia

import java.time.Instant

import org.scalacheck.{Arbitrary, Gen}

trait Arbitraries {
  implicit val arbitraryInstant: Arbitrary[Instant] = Arbitrary[Instant] {
    Gen.calendar.map(_.toInstant)
  }

  implicit val arbitrarySymbol: Arbitrary[Symbol] = Arbitrary[Symbol] {
    Gen.asciiStr.map(Symbol(_))
  }
}

object Arbitraries extends Arbitraries
