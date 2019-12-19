package medeia

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.{EitherValues, OptionValues}

trait MedeiaSpec extends AnyFlatSpecLike with TypeCheckedTripleEquals with Matchers with EitherValues with OptionValues
