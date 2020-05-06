package medeia.generic

package object auto {
  type AutoDerivationUnlocked[_] = AutoDerivationUnlocker
  implicit val unlocker: AutoDerivationUnlocker = new AutoDerivationUnlocker {}
}

private[medeia] sealed abstract class AutoDerivationUnlocker
