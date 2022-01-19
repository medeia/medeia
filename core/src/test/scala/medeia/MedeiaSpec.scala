package medeia

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues

trait MedeiaSpec extends AnyFlatSpecLike with TypeCheckedTripleEquals with Matchers with EitherValues
