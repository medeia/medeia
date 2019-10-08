inThisBuild(
  List(
    organization := "de.megaera",
    homepage := Some(url("https://github.com/medeia/medeia")),
    licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer("froth", "Frederick Roth", "f-roth@megaera.de", url("https://derfred.org")),
      Developer("markus1189", "Markus Hauck", "markus1189@gmail.com", url("https://github.com/markus1189"))
    )
  ))

import scala.xml.{Elem, Node => XmlNode, NodeSeq => XmlNodeSeq}
import scala.xml.transform.{RewriteRule, RuleTransformer}

lazy val root = (project in file("."))
  .settings(name := "medeia", organization := "de.megaera", crossScalaVersions := List("2.12.10", "2.13.1"))
  .settings(Dependencies.Libraries, Dependencies.TestLibraries)
  .enablePlugins(MiscSettingsPlugin)

lazy val publishSettings = Seq(
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  publishArtifact in Test := false,
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
          case _                               => node
        }
      }
    ).transform(node).head
  }
)
