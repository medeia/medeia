package medeia.decoder

import org.bson.BsonType

trait BsonDecoderError extends Exception

object BsonDecoderError {
  final case class TypeMismatch(actual: BsonType, expected: BsonType)
      extends Exception(s"expected: ${expected.toString}, actual: ${actual.toString}")
      with BsonDecoderError

  final case class KeyNotFound(keyName: String) extends Exception(s"Key not found: $keyName") with BsonDecoderError

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  final case class FieldParseError(message: String, cause: Exception = null) extends Exception(message, cause) with BsonDecoderError

  final case class InvalidTypeTag(typeTag: String)
      extends Exception(s"Trying to decode sealed trait, but no match found for typetag: $typeTag")
      with BsonDecoderError

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  final case class GenericDecoderError(message: String, cause: Exception = null) extends Exception(message, cause) with BsonDecoderError
}
