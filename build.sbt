import ReleaseTransformations._
import scala.xml.{Elem, Node => XmlNode, NodeSeq => XmlNodeSeq}
import scala.xml.transform.{RewriteRule, RuleTransformer}

lazy val root = (project in file("."))
  .settings(name := "medeia", organization := "de.megaera")
  .settings(Dependencies.Libraries, Dependencies.TestLibraries)
  .enablePlugins(MiscSettingsPlugin)

lazy val publishSettings = Seq(
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  homepage := Some(url("https://github.com/medeia/medeia")),
  licenses := Seq(
    "Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ =>
    false
  },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/medeia/medeia"),
      "scm:git:git@github.com:medeia/medeia.git"
    )
  ),
  developers := List(
    Developer("froth",
              "Frederick Roth",
              "f-roth@megaera.de",
              url("https://derfred.org"))
  ),
  pomPostProcess := { (node: XmlNode) =>
    new RuleTransformer(
      new RewriteRule {
        private def isTestScope(elem: Elem): Boolean =
          elem.label == "dependency" && elem.child.exists(child =>
            child.label == "scope" && child.text == "test")

        override def transform(node: XmlNode): XmlNodeSeq = node match {
          case elem: Elem if isTestScope(elem) => Nil
          case _                               => node
        }
      }
    ).transform(node).head
  }
)
