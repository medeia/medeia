import sbt.Keys._
import sbt._

object Dependencies {
  object Versions {
    val scalaTest = "3.0.8"
    val mongoScalaBson = "2.7.0"
    val cats = "2.0.0"
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
