package medeia.generic

final case class GenericDerivationOptions[A](keyTransformation: PartialFunction[String, String] = PartialFunction.empty) {
  val transformKeys: String => String = keyTransformation.orElse { case x => x }
}
