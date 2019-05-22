package medeia.decoder

import org.bson.BsonType

sealed abstract class BsonDecoderError extends Exception

object BsonDecoderError {
  case class TypeMismatch(actual: BsonType, expected: BsonType)
      extends BsonDecoderError

  case class KeyNotFound(keyName: String) extends BsonDecoderError
}
