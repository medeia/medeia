package medeia.enumeratum

import enumeratum.values.{IntEnum, IntEnumEntry}

import scala.collection.immutable

sealed abstract class TestIntEnum(val value: Int) extends IntEnumEntry

object TestIntEnum extends IntEnum[TestIntEnum] {
  val values: immutable.IndexedSeq[TestIntEnum] = findValues
  case object Entry1 extends TestIntEnum(1)
  case object Entry2 extends TestIntEnum(2)
}
