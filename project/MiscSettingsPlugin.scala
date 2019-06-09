import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport._
import com.lucidchart.sbt.scalafmt.ScalafmtPlugin
import sbt.Keys._
import sbt.{Def, _}

object MiscSettingsPlugin extends AutoPlugin {
  override def requires: Plugins = ScalafmtPlugin

  override lazy val projectSettings: Seq[Def.Setting[_]] = commonSettings

  lazy val commonSettings = Seq(
    organization := "",
    scalaVersion := "2.12.8",
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-target:jvm-1.8",
      "-encoding",
      "UTF-8",
      "-feature", // warning and location for usages of features that should be imported explicitly
      "-unchecked", // additional warnings where generated code depends on assumptions
      "-Xlint", // recommended additional warnings
      "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
      "-Ywarn-value-discard", // Warn when non-Unit expression results are unused
      "-Ywarn-inaccessible",
      "-language:reflectiveCalls",
      "-Ypartial-unification"
    ),
    scalafmtOnCompile := true
  )
}
