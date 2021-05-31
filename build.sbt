import sbt.Keys.versionScheme
import wartremover.Wart
import wartremover.WartRemover.autoImport.{Warts, wartremoverWarnings}

import scala.xml.{Elem, Node => XmlNode, NodeSeq => XmlNodeSeq}
import scala.xml.transform.{RewriteRule, RuleTransformer}

inThisBuild(
  List(
    organization := "de.megaera",
    homepage := Some(url("https://github.com/medeia/medeia")),
    licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer("froth", "Frederick Roth", "f-roth@megaera.de", url("https://derfred.org")),
      Developer("markus1189", "Markus Hauck", "markus1189@gmail.com", url("https://github.com/markus1189"))
    ),
    versionScheme := Some("semver-spec"),
    semanticdbEnabled := true,
    semanticdbVersion := "4.4.20"
  ))

ThisBuild / crossScalaVersions := List("2.12.14", "2.13.6")

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(RefPredicate.Equals(Ref.Branch("main")), RefPredicate.StartsWith(Ref.Tag("v")))

ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    List("ci-release"),
    env = Map(
      "PGP_PASSPHRASE" -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET" -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
    )
  )
)

val wartIgnoreMain: List[Wart] = List(Wart.Any, Wart.Nothing, Wart.DefaultArguments)
val wartIgnoreTest = wartIgnoreMain ++ List(Wart.FinalCaseClass, Wart.NonUnitStatements, Wart.LeakingSealed, Wart.PlatformDefault, Wart.Equals)
lazy val commonSettings = List(
  organization := "de.megaera",
  crossScalaVersions := List("2.12.14", "2.13.6"),
  versionScheme := Some("semver-spec"),
  Compile / compile / wartremoverWarnings := Warts.allBut(wartIgnoreMain: _*),
  Test / compile / wartremoverWarnings := Warts.allBut(wartIgnoreTest: _*)
)

lazy val core = (project in file("core/"))
  .settings(commonSettings)
  .enablePlugins(MiscSettingsPlugin)

lazy val enumeratum = (project in file("modules/enumeratum/"))
  .settings(commonSettings)
  .enablePlugins(MiscSettingsPlugin)
  .dependsOn(core)

lazy val refined = (project in file("modules/refined/"))
  .settings(commonSettings)
  .enablePlugins(MiscSettingsPlugin)
  .dependsOn(core)

lazy val publishSettings = Seq(
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  Test / publishArtifact := false,
  pomIncludeRepository := { _ =>
    false
  },
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/medeia/medeia"),
      "scm:git:git@github.com:medeia/medeia.git"
    )
  ),
  pomPostProcess := { node: XmlNode =>
    new RuleTransformer(
      new RewriteRule {
        private def isTestScope(elem: Elem): Boolean =
          elem.label == "dependency" && elem.child.exists(child => child.label == "scope" && child.text == "test")

        override def transform(node: XmlNode): XmlNodeSeq = node match {
          case elem: Elem if isTestScope(elem) => Nil
          case _ => node
        }
      }
    ).transform(node).head
  }
)
