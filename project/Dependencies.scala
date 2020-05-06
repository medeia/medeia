import sbt.Keys._
import sbt._

object Dependencies {
  object Versions {
    val scalaTest = "3.1.1"
    val mongoScalaBson = "4.0.3"
    val cats = "2.1.1"
    val scalaCheck = "1.14.3"
    val shapeless = "2.3.3"
    val scalaCollectionCompatVersion = "2.1.6"
    val enumeratumVersion = "1.6.0"
  }

  lazy val Libraries: Vector[ModuleID] =  Vector(
    "org.mongodb.scala" %% "mongo-scala-bson" % Versions.mongoScalaBson,
    "org.typelevel" %% "cats-core" % Versions.cats,
    "com.chuusai" %% "shapeless" % Versions.shapeless,
    "org.scala-lang.modules" %% "scala-collection-compat" % Versions.scalaCollectionCompatVersion
  )

  lazy val TestLibraries: Vector[ModuleID] = Vector(
    "org.scalatest" %% "scalatest" % Versions.scalaTest,
    "org.scalacheck" %% "scalacheck" % Versions.scalaCheck
  ).map(_ % Test)

  lazy val EnumeratumLibraries: Vector[ModuleID] = Vector(
    "com.beachape" %% "enumeratum" % Versions.enumeratumVersion
  )

  lazy val EnumeratumTestLibraries: Vector[ModuleID] = Vector(
    "com.beachape" %% "enumeratum-scalacheck" % Versions.enumeratumVersion
  )
}
