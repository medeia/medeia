package medeia.generic.semiauto

import medeia.BsonCodec
import medeia.decoder.BsonDecoder
import medeia.encoder.BsonDocumentEncoder
import medeia.generic.{GenericDecoder, GenericEncoder, ShapelessDecoder}
import shapeless.{LabelledGeneric, Lazy}

trait Semiauto {
  def deriveEncoder[A](implicit encoder: GenericEncoder[A]): BsonDocumentEncoder[A] =
    encoder

  def deriveDecoder[A, H](implicit
                          generic: LabelledGeneric.Aux[A, H],
                          hDecoder: Lazy[ShapelessDecoder[A, H]]): BsonDecoder[A] =
    GenericDecoder.genericDecoder

  def deriveCodec[A, H](implicit
                        encoder: GenericEncoder[A],
                        generic: LabelledGeneric.Aux[A, H],
                        hDecoder: Lazy[ShapelessDecoder[A, H]]): BsonCodec[A] = {
    import GenericDecoder.genericDecoder
    BsonCodec.fromEncoderAndDecoder
  }
}
