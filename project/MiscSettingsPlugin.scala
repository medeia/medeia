import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport._
import com.lucidchart.sbt.scalafmt.ScalafmtPlugin
import sbt.Keys._
import sbt.{Def, _}

object MiscSettingsPlugin extends AutoPlugin {
  override def requires: Plugins = ScalafmtPlugin

  override lazy val projectSettings: Seq[Def.Setting[_]] = commonSettings

  lazy val extraScalacOptions = Seq(
    "-unchecked",
    "-deprecation",
    "-language:_",
    "-target:jvm-1.8",
    "-encoding",
    "UTF-8",
    "-feature", // warning and location for usages of features that should be imported explicitly
    "-unchecked", // additional warnings where generated code depends on assumptions
    "-Xlint", // recommended additional warnings
    "-Ywarn-value-discard", // Warn when non-Unit expression results are unused
    "-language:reflectiveCalls"
  )

  lazy val extraScalacOptions_2_12 = Seq(
    "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
    "-Ywarn-inaccessible",
    "-Ypartial-unification"
  )

  lazy val commonSettings = Seq(
    organization := "",
    scalaVersion := "2.13.4",
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 12)) => extraScalacOptions ++ extraScalacOptions_2_12
        case Some((2, 13)) => extraScalacOptions
        case _ => extraScalacOptions
      }
    },
    scalafmtOnCompile := true
  )
}
