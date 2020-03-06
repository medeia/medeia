package medeia.generic

case class SealedTraitDerivationOptions[A](
    discriminatorTransformation: PartialFunction[String, String] = PartialFunction.empty,
    discriminatorKey: String = "type"
) {
  val transformDiscriminator: String => String = discriminatorTransformation.orElse { case x => x }
}
