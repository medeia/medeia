package medeia

import cats.syntax.either._
import eu.timepit.refined.api.{Refined, Validate}
import eu.timepit.refined.refineV
import medeia.decoder.BsonDecoder
import medeia.decoder.BsonDecoderError.GenericDecoderError
import medeia.encoder.BsonEncoder

package object refined {
  implicit def encoder[T, P](implicit innerEncoder: BsonEncoder[T]): BsonEncoder[Refined[T, P]] =
    innerEncoder.contramap(_.value)

  implicit def decoder[T, P](implicit innerDecoder: BsonDecoder[T], validate: Validate[T, P]): BsonDecoder[Refined[T, P]] =
    bson => innerDecoder.decode(bson).flatMap(t => refineV[P](t).leftMap(error => GenericDecoderError(error)).toEitherNec)
}
