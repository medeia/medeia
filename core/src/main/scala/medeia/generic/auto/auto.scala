package medeia.generic

import scala.deprecated

package object auto {
  type AutoDerivationUnlocked[_] = AutoDerivationUnlocker
  @deprecated("medeia.generic.auto will be removed in 1.0 - migrate to BsonCodec.derived", "medeia 0.15.1")
  implicit val unlocker: AutoDerivationUnlocker = new AutoDerivationUnlocker {}
}

private[medeia] sealed abstract class AutoDerivationUnlocker
