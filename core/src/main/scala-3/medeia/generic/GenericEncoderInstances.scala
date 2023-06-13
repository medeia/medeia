package medeia.generic

import shapeless3.deriving.*

trait GenericEncoderInstances {
  @SuppressWarnings(Array("org.wartremover.warts.ImplicitConversion"))
  implicit inline def genericEncoder[A](using
      gen: K0.Generic[A],
      gdOptions: GenericDerivationOptions[A] = GenericDerivationOptions[A](),
      stOptions: SealedTraitDerivationOptions[A] = SealedTraitDerivationOptions[A]()
  ): GenericEncoder[A] =
    gen.derive(GenericProductEncoder.encoder, GenericCoproductEncoder.encoder)
}
