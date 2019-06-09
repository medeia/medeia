import sbt.Keys._
import sbt._

object Dependencies {
  object Versions {
    val scalaTest = "3.0.5"
    val mongoScalaBson = "2.6.0"
    val cats = "1.6.1"
    val scalaCheck = "1.14.0"
    val shapeless = "2.3.3"
  }

  lazy val Libraries = libraryDependencies ++= Vector(
    "org.mongodb.scala" %% "mongo-scala-bson" % Versions.mongoScalaBson,
    "org.typelevel" %% "cats-core" % Versions.cats,
    "com.chuusai" %% "shapeless" % Versions.shapeless
  )

  lazy val TestLibraries = libraryDependencies ++= Vector(
    "org.scalatest" %% "scalatest" % Versions.scalaTest,
    "org.scalacheck" %% "scalacheck" % Versions.scalaCheck
  ).map(_ % Test)
}
