package medeia.generic

final case class SealedTraitDerivationOptions[A](
    discriminatorTransformation: PartialFunction[String, String] = PartialFunction.empty,
    discriminatorKey: String = "type"
) {
  val transformDiscriminator: String => String = discriminatorTransformation.orElse { case x => x }
}
