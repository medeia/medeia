package medeia.enumeratum

import enumeratum.Enum
import enumeratum.EnumEntry

import scala.collection.immutable

sealed abstract class TestEnum(override val entryName: String) extends EnumEntry

object TestEnum extends Enum[TestEnum] {
  override val values: immutable.IndexedSeq[TestEnum] = findValues

  case object A extends TestEnum("A")
  case object B extends TestEnum("B")
  case object C extends TestEnum("C")
}
