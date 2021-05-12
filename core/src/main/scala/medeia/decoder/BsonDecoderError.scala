package medeia.decoder

import medeia.decoder.BsonDecoderError._
import org.bson.BsonType

sealed trait BsonDecoderError extends Exception {
  def stack: ErrorStack

  final def push(frame: StackFrame): BsonDecoderError =
    this match {
      case err @ TypeMismatch(_, _, _)        => err.copy(stack = err.stack.push(frame))
      case err @ KeyNotFound(_, _)            => err.copy(stack = err.stack.push(frame))
      case err @ FieldParseError(_, _, _)     => err.copy(stack = err.stack.push(frame))
      case err @ InvalidTypeTag(_, _)         => err.copy(stack = err.stack.push(frame))
      case err @ GenericDecoderError(_, _, _) => err.copy(stack = err.stack.push(frame))
    }
}

object BsonDecoderError {
  final case class TypeMismatch(actual: BsonType, expected: BsonType, stack: ErrorStack = ErrorStack.empty)
      extends Exception(s"expected: ${expected.toString}, actual: ${actual.toString}, stack: ${stack.toString}")
      with BsonDecoderError

  final case class KeyNotFound(keyName: String, stack: ErrorStack = ErrorStack.empty)
      extends Exception(s"Key not found: $keyName, stack: ${stack.toString}")
      with BsonDecoderError

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  final case class FieldParseError(message: String, cause: Exception = null, stack: ErrorStack = ErrorStack.empty)
      extends Exception(s"$message, stack: ${stack.toString}", cause)
      with BsonDecoderError

  final case class InvalidTypeTag(typeTag: String, stack: ErrorStack = ErrorStack.empty)
      extends Exception(s"Trying to decode sealed trait, but no match found for typetag: $typeTag")
      with BsonDecoderError

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  final case class GenericDecoderError(message: String, cause: Exception = null, stack: ErrorStack = ErrorStack.empty)
      extends Exception(s"$message, stack: ${stack.toString}", cause)
      with BsonDecoderError
}
