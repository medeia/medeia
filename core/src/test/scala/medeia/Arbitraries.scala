package medeia

import java.time.Instant

import cats.data.{Chain, NonEmptyChain, NonEmptyList}
import org.scalacheck.{Arbitrary, Gen}

trait Arbitraries {
  implicit val arbitraryInstant: Arbitrary[Instant] = Arbitrary[Instant] {
    Gen.calendar.map(_.toInstant)
  }

  implicit val arbitrarySymbol: Arbitrary[Symbol] = Arbitrary[Symbol] {
    Gen.asciiStr.map(Symbol(_))
  }

  implicit def arbitraryOption[A](implicit arbitraryA: Arbitrary[A]): Arbitrary[Option[A]] =
    Arbitrary[Option[A]] {
      Gen.option(arbitraryA.arbitrary)
    }

  implicit def arbitraryList[A](implicit arbitraryA: Arbitrary[A]): Arbitrary[List[A]] =
    Arbitrary[List[A]] {
      Gen.listOf(arbitraryA.arbitrary)
    }

  implicit def arbitrartChain[A](implicit arbitraryA: Arbitrary[A]): Arbitrary[Chain[A]] =
    Arbitrary[Chain[A]] {
      Gen.listOf(arbitraryA.arbitrary).map(Chain.fromSeq)
    }

  implicit def arbitraryNonEmptyList[A](implicit arbitraryA: Arbitrary[A]): Arbitrary[NonEmptyList[A]] =
    Arbitrary[NonEmptyList[A]] {
      Gen.nonEmptyListOf(arbitraryA.arbitrary).map(NonEmptyList.fromListUnsafe)
    }

  implicit def arbitrartNonEmptyChain[A](implicit arbitraryA: Arbitrary[A]): Arbitrary[NonEmptyChain[A]] =
    Arbitrary[NonEmptyChain[A]] {
      arbitraryNonEmptyList[A].arbitrary.map(NonEmptyChain.fromNonEmptyList)
    }
}

object Arbitraries extends Arbitraries
