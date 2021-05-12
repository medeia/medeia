package medeia.decoder

final case class ErrorStack(frames: List[StackFrame]) {
  def push(frame: StackFrame): ErrorStack = ErrorStack(frame +: frames)

  override def toString: String =
    frames.mkString("ErrorStack(", " -> ", ")")
}

object ErrorStack {
  val empty: ErrorStack = ErrorStack(List.empty)
}

sealed trait StackFrame extends Product with Serializable
object StackFrame {
  final case class Attr(name: String) extends StackFrame
  final case class Case(name: String) extends StackFrame
  final case class Index(value: Int) extends StackFrame
  final case class MapKey[A](value: A) extends StackFrame
  final case class Custom(name: String) extends StackFrame
}
