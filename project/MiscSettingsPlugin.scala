import org.scalafmt.sbt.ScalafmtPlugin
import sbt.Keys._
import sbt.{CrossVersion, Def, _}
import scalafix.sbt.ScalafixPlugin.autoImport.{scalafixOnCompile, scalafixScalaBinaryVersion}

object MiscSettingsPlugin extends AutoPlugin {
  override def requires: Plugins = ScalafmtPlugin

  override lazy val projectSettings: Seq[Def.Setting[_]] = commonSettings ++ scalafixSettings

  lazy val extraScalacOptions = Seq(
    "-unchecked",
    "-deprecation",
    "-language:_",
    "-encoding",
    "UTF-8",
    "-feature", // warning and location for usages of features that should be imported explicitly
    "-unchecked", // additional warnings where generated code depends on assumptions
    "-language:reflectiveCalls"
  )

  lazy val extraScalacOptions_2 = Seq(
    "-target:jvm-1.8",
    "-Xlint", // recommended additional warnings
    "-Ywarn-value-discard" // Warn when non-Unit expression results are unused
  )

  lazy val extraScalacOptions_2_12 = Seq(
    "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
    "-Ywarn-inaccessible",
    "-Ypartial-unification"
  )

  lazy val extraScalacOptions_3 = Seq(
    "-Xtarget:jvm-1.8",
    "-deprecation"
  )

  lazy val commonSettings = Seq(
    organization := "",
    scalaVersion := "2.13.10",
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 12)) => extraScalacOptions ++ extraScalacOptions_2 ++ extraScalacOptions_2_12
        case Some((2, 13)) => extraScalacOptions ++ extraScalacOptions_2
        case _             => extraScalacOptions
      }
    }
  )

  lazy val scalafixSettings = Seq(
    ThisBuild / scalafixScalaBinaryVersion := CrossVersion.binaryScalaVersion(scalaVersion.value)
  )
}
