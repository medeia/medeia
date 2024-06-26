package medeia.generic

import shapeless3.deriving.*

trait GenericDecoderInstances {
  @SuppressWarnings(Array("org.wartremover.warts.ImplicitConversion"))
  implicit inline def genericDecoder[A](using
      gen: K0.Generic[A],
      gdOptions: GenericDerivationOptions[A] = GenericDerivationOptions[A](),
      stOptions: SealedTraitDerivationOptions[A] = SealedTraitDerivationOptions[A]()
  ): GenericDecoder[A] =
    gen.derive(GenericProductDecoder.decoder, GenericCoproductDecoder.decoder)
}
