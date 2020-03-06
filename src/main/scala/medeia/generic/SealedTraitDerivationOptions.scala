package medeia.generic

case class SealedTraitDerivationOptions[A](
    typeNameTransformation: PartialFunction[String, String] = PartialFunction.empty,
    typeTag: String = "type"
) {
  val transformTypeNames: String => String = typeNameTransformation.orElse { case x => x }
}
