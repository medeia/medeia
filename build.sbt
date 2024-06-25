import sbt.Keys.versionScheme
import wartremover.Wart
import wartremover.WartRemover.autoImport.{Warts, wartremoverWarnings}

inThisBuild(
  List(
    scalaVersion := "2.13.14",
    organization := "de.megaera",
    homepage := Some(url("https://github.com/medeia/medeia")),
    licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer("froth", "Frederick Roth", "f-roth@megaera.de", url("https://derfred.org")),
      Developer("markus1189", "Markus Hauck", "markus1189@gmail.com", url("https://github.com/markus1189"))
    ),
    versionScheme := Some("early-semver"),
    scalafixScalaBinaryVersion := CrossVersion.binaryScalaVersion(scalaVersion.value),
    semanticdbEnabled := true,
    semanticdbVersion := "4.9.7"
  )
)

ThisBuild / crossScalaVersions := List("2.12.19", "2.13.14", "3.3.3")

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(RefPredicate.Equals(Ref.Branch("main")), RefPredicate.StartsWith(Ref.Tag("v")))
ThisBuild / tlBaseVersion := "0.13"
ThisBuild / tlSonatypeUseLegacyHost := true
ThisBuild / tlCiScalafmtCheck := true
ThisBuild / tlCiScalafixCheck := true
ThisBuild / tlCiHeaderCheck := false
ThisBuild / tlFatalWarnings := false

val wartIgnoreMain: List[Wart] = List(Wart.Any, Wart.Nothing, Wart.DefaultArguments, Wart.ImplicitParameter, Wart.Equals)
val wartIgnoreTest = wartIgnoreMain ++ List(Wart.FinalCaseClass, Wart.NonUnitStatements, Wart.LeakingSealed, Wart.PlatformDefault)
lazy val wartRemoverSettings = List(
  Compile / compile / wartremoverWarnings := Warts.allBut(wartIgnoreMain: _*),
  Test / compile / wartremoverWarnings := Warts.allBut(wartIgnoreTest: _*)
)

lazy val core = (project in file("core/"))
  .settings(wartRemoverSettings)

lazy val enumeratum = (project in file("modules/enumeratum/"))
  .settings(wartRemoverSettings)
  .settings(scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, n)) => List("-Yretain-trees")
      case _            => List.empty
    }
  })
  .dependsOn(core)

lazy val refined = (project in file("modules/refined/"))
  .settings(wartRemoverSettings)
  .dependsOn(core)
