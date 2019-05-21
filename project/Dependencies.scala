import sbt._
import sbt.Keys._

object Dependencies {
  object Versions {
    val scalaTest = "3.0.5"
  }

  lazy val Libraries = libraryDependencies ++= Vector(
  )

  lazy val TestLibraries = libraryDependencies ++= Vector(
    "org.scalatest" %% "scalatest" % Versions.scalaTest,
  ).map(_ % Test)
}
