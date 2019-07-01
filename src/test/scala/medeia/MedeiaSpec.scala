package medeia

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{EitherValues, FlatSpecLike, Matchers, OptionValues}

trait MedeiaSpec extends FlatSpecLike with TypeCheckedTripleEquals with Matchers with EitherValues with OptionValues
