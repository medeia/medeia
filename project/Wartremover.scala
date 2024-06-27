import wartremover.Wart
object Wartremover {
  val wartIgnoreMain: List[Wart] = List(Wart.Any, Wart.Nothing, Wart.DefaultArguments, Wart.ImplicitParameter, Wart.Equals)
  val wartIgnoreTest = wartIgnoreMain ++ List(Wart.FinalCaseClass, Wart.NonUnitStatements, Wart.LeakingSealed, Wart.PlatformDefault)
}
