package medeia.decoder

import org.bson.BsonType

sealed abstract class BsonDecoderError extends Exception

object BsonDecoderError {
  case class TypeMismatch(actual: BsonType, expected: BsonType) extends BsonDecoderError {
    override def toString: String = s"expected: $expected, actual: $actual"

  }

  case class KeyNotFound(keyName: String) extends BsonDecoderError

  case class FieldParseError(error: Exception) extends BsonDecoderError
}
