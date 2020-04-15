import sbt.Keys._
import sbt._

object Dependencies {
  object Versions {
    val scalaTest = "3.1.1"
    val mongoScalaBson = "2.9.0"
    val cats = "2.1.1"
    val scalaCheck = "1.14.3"
    val shapeless = "2.3.3"
    val scalaCollectionCompatVersion = "2.1.5"
  }

  lazy val Libraries = libraryDependencies ++= Vector(
    "org.mongodb.scala" %% "mongo-scala-bson" % Versions.mongoScalaBson,
    "org.typelevel" %% "cats-core" % Versions.cats,
    "com.chuusai" %% "shapeless" % Versions.shapeless,
    "org.scala-lang.modules" %% "scala-collection-compat" % Versions.scalaCollectionCompatVersion
  )

  lazy val TestLibraries = libraryDependencies ++= Vector(
    "org.scalatest" %% "scalatest" % Versions.scalaTest,
    "org.scalacheck" %% "scalacheck" % Versions.scalaCheck
  ).map(_ % Test)
}
