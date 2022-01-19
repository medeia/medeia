import sbt.Keys._
import sbt._

object Dependencies {
  object Versions {
    val scalaTest = "3.2.11"
    val mongoScalaBson = "4.4.1"
    val cats = "2.7.0"
    val scalaCheck = "1.15.4"
    val shapeless2 = "2.3.7"
    val shapeless3 = "3.0.3"
    val scalaCollectionCompatVersion = "2.6.0"
    val enumeratumVersion = "1.7.0"
    val refinedVersion = "0.9.28"
  }

  lazy val Libraries: Vector[ModuleID] = Vector(
    ("org.mongodb.scala" %% "mongo-scala-bson" % Versions.mongoScalaBson).cross(CrossVersion.for3Use2_13),
    "org.typelevel" %% "cats-core" % Versions.cats,
    ("org.scala-lang.modules" %% "scala-collection-compat" % Versions.scalaCollectionCompatVersion).cross(CrossVersion.for3Use2_13)
  )

  lazy val Scala2Libraries: Vector[ModuleID] = Vector(
    "com.chuusai" %% "shapeless" % Versions.shapeless2
  )

  lazy val Scala3Libraries: Vector[ModuleID] = Vector(
    "org.typelevel" %% "shapeless3-deriving" % Versions.shapeless3
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
  ).map(_ % Test)

  lazy val RefinedLibraries: Vector[ModuleID] = Vector(
    "eu.timepit" %% "refined" % Versions.refinedVersion
  )

  lazy val RefinedTestLibraries: Vector[ModuleID] = Vector(
    "eu.timepit" %% "refined-scalacheck" % Versions.refinedVersion
  ).map(_ % Test)
}
