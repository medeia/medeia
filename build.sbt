inThisBuild(
  List(
    scalaVersion := "2.13.14",
    crossScalaVersions := List("2.12.19", "2.13.14", "3.3.3"),
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

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(RefPredicate.Equals(Ref.Branch("main")), RefPredicate.StartsWith(Ref.Tag("v")))

ThisBuild / tlBaseVersion := "0.13"
ThisBuild / tlSonatypeUseLegacyHost := true
ThisBuild / tlCiScalafmtCheck := true
ThisBuild / tlCiScalafixCheck := true
ThisBuild / tlCiHeaderCheck := false
ThisBuild / tlFatalWarnings := false

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
