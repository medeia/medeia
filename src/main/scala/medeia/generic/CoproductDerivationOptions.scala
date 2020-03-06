package medeia.generic

case class CoproductDerivationOptions[-A](
    typeNameTransformation: PartialFunction[String, String] = PartialFunction.empty,
    typeNameKey: String = "type"
) {
  val transformTypeNames: String => String = typeNameTransformation.orElse { case x => x }
}
