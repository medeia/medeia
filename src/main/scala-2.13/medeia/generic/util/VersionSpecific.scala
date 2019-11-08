package medeia.generic.util

object VersionSpecific{
  trait Lazy[+A] extends Serializable {
    def value(): A
  }

  object Lazy {
    implicit def instance[A](implicit ev: => A): Lazy[A] = () => ev
  }
}
