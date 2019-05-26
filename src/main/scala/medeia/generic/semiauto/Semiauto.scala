package medeia.generic.semiauto

import medeia.encoder.BsonEncoder
import medeia.generic.GenericEncoder

trait Semiauto {
  def deriveEncoder[A](implicit enc: GenericEncoder[A]): BsonEncoder[A] =
    enc
}
