package medeia.generic.util

object VersionSpecific{
  type Lazy[+A] = shapeless.Lazy[A]
}
