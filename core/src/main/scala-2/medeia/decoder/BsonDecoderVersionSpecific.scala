package medeia.decoder

import medeia.generic.GenericDecoder

trait BsonDecoderVersionSpecific {
  def derived[A](implicit genericDecoder: GenericDecoder[A]): BsonDecoder[A] = genericDecoder
}
