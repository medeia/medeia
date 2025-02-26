val scala2_13 = "2.13.16"

inThisBuild(
  List(
    scalaVersion := scala2_13,
    crossScalaVersions := List(scala2_13, "3.3.5"),
    organization := "de.megaera",
    homepage := Some(url("https://github.com/medeia/medeia")),
    licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer("froth", "Frederick Roth", "f-roth@megaera.de", url("https://derfred.org")),
      Developer("markus1189", "Markus Hauck", "markus1189@gmail.com", url("https://github.com/markus1189"))
    ),
    versionScheme := Some("early-semver"),
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(RefPredicate.Equals(Ref.Branch("main")), RefPredicate.StartsWith(Ref.Tag("v")))

ThisBuild / tlBaseVersion := "1.0"
ThisBuild / sonatypeCredentialHost := Sonatype.sonatypeLegacy
ThisBuild / tlCiScalafmtCheck := true
ThisBuild / tlCiScalafixCheck := true
ThisBuild / tlCiHeaderCheck := false

lazy val root = tlCrossRootProject
  .aggregate(core, enumeratum, refined)

lazy val core = project
  .in(file("core/"))

lazy val enumeratum = project
  .in(file("modules/enumeratum/"))
  .dependsOn(core)

lazy val refined = project
  .in(file("modules/refined/"))
  .dependsOn(core)
