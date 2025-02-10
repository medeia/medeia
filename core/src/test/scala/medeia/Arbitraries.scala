package medeia

import cats.Order
import cats.data.{Chain, NonEmptyChain, NonEmptyList, NonEmptyMap, NonEmptySet}
import org.scalacheck.{Arbitrary, Gen}

import java.net.URI
import java.time.Instant
import java.util.Locale
import scala.collection.immutable.SortedMap

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

  implicit def arbitraryChain[A](implicit arbitraryA: Arbitrary[A]): Arbitrary[Chain[A]] =
    Arbitrary[Chain[A]] {
      Gen.listOf(arbitraryA.arbitrary).map(Chain.fromSeq)
    }

  implicit def arbitraryNonEmptyList[A](implicit arbitraryA: Arbitrary[A]): Arbitrary[NonEmptyList[A]] =
    Arbitrary[NonEmptyList[A]] {
      Gen.nonEmptyListOf(arbitraryA.arbitrary).map(NonEmptyList.fromListUnsafe)
    }

  implicit def arbitraryNonEmptyChain[A](implicit arbitraryA: Arbitrary[A]): Arbitrary[NonEmptyChain[A]] =
    Arbitrary[NonEmptyChain[A]] {
      arbitraryNonEmptyList[A].arbitrary.map(NonEmptyChain.fromNonEmptyList)
    }

  implicit def arbitraryNonEmptySet[A: Order](implicit arbitraryA: Arbitrary[A]): Arbitrary[NonEmptySet[A]] =
    Arbitrary[NonEmptySet[A]] {
      arbitraryNonEmptyList[A].arbitrary.map(as => NonEmptySet.of(as.head, as.tail: _*))
    }

  implicit def arbitraryNonEmptyMap[K: Ordering, A](implicit arbitraryK: Arbitrary[K], arbitraryA: Arbitrary[A]): Arbitrary[NonEmptyMap[K, A]] =
    Arbitrary[NonEmptyMap[K, A]] {
      Gen
        .nonEmptyMap(
          for {
            k <- arbitraryK.arbitrary
            a <- arbitraryA.arbitrary
          } yield (k, a)
        )
        .map(map => NonEmptyMap.fromMapUnsafe(SortedMap.from(map)))
    }

  implicit val aribtraryLocale: Arbitrary[Locale] =
    Arbitrary {
      Gen.oneOf(Locale.forLanguageTag("de-AT"), Locale.GERMANY, Locale.ENGLISH, new Locale("da"))
    }

  implicit val aribtraryUri: Arbitrary[URI] =
    Arbitrary {
      Gen.oneOf(URI.create("https://example.com"), URI.create("/test/"), URI.create("https://example.com:8080/test/path?my=param"))
    }
}

object Arbitraries extends Arbitraries
