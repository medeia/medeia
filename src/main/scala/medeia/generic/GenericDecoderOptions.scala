package medeia.generic

case class GenericDecoderOptions[A](keyTransformation: PartialFunction[String, String] = PartialFunction.empty) {
  val transformKeys: String => String = keyTransformation.orElse { case x => x }
}
