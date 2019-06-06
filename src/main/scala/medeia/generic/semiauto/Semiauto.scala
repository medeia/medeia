package medeia.generic.semiauto

import medeia.BsonCodec
import medeia.decoder.BsonDecoder
import medeia.encoder.BsonDocumentEncoder
import medeia.generic.{GenericDecoder, GenericEncodingOptions, GenericEncoder, ShapelessDecoder}
import shapeless.{LabelledGeneric, Lazy}

trait Semiauto {
  def deriveEncoder[A](implicit encoder: GenericEncoder[A],
                       options: GenericEncodingOptions[A] = GenericEncodingOptions[A]()): BsonDocumentEncoder[A] =
    encoder

  def deriveDecoder[A, H](implicit
                          generic: LabelledGeneric.Aux[A, H],
                          hDecoder: Lazy[ShapelessDecoder[A, H]],
                          options: GenericEncodingOptions[A] = GenericEncodingOptions[A]()): BsonDecoder[A] =
    GenericDecoder.genericDecoder

  def deriveCodec[A, H](implicit
                        encoder: GenericEncoder[A],
                        generic: LabelledGeneric.Aux[A, H],
                        hDecoder: Lazy[ShapelessDecoder[A, H]],
                        options: GenericEncodingOptions[A] = GenericEncodingOptions[A]()): BsonCodec[A] = {
    import GenericDecoder.genericDecoder
    BsonCodec.fromEncoderAndDecoder
  }
}
