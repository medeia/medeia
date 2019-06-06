package medeia.generic

case class GenericEncodingOptions[A](keyTransformation: PartialFunction[String, String] = PartialFunction.empty) {
  val transformKeys: String => String = keyTransformation.orElse { case x => x }
}
