package medeia.generic

final case class GenericDerivationOptions[A](
    keyTransformation: PartialFunction[String, String] = PartialFunction.empty,
    discriminatorTransformation: PartialFunction[String, String] = PartialFunction.empty,
    discriminatorKey: String = "type"
) {
  val transformKeys: String => String = keyTransformation.orElse { case x => x }
  val transformDiscriminator: String => String = discriminatorTransformation.orElse { case x => x }
}
