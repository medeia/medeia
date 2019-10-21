package medeia.decoder

import org.bson.BsonType

sealed trait BsonDecoderError

object BsonDecoderError {
  case class TypeMismatch(actual: BsonType, expected: BsonType) extends Exception(s"expected: $expected, actual: $actual") with BsonDecoderError

  case class KeyNotFound(keyName: String) extends Exception(s"Key not found: $keyName") with BsonDecoderError

  case class FieldParseError(message: String, cause: Exception = None.orNull) extends Exception(message, cause) with BsonDecoderError

  case class InvalidTypeTag(typeTag: String)
      extends Exception(s"Trying to decode sealed trait, but no match found for typetag: $typeTag")
      with BsonDecoderError
}
