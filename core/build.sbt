import Dependencies._
import Wartremover._

name := "medeia"

scalacOptions ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((3, _)) =>
      List(
        // Suppress false positive E198 warnings for type parameters in polymorphic functions
        // used with shapeless3 K0 API (inject, unfold, project, fold)
        // Tracked in: https://github.com/scala/scala3/issues/24665 (fixed in PR #25505, will be in 3.3.8)
        "-Wconf:src=.*GenericDecoderInstances\\.scala&msg=unused private member:s",
        "-Wconf:src=.*GenericEncoderInstances\\.scala&msg=unused private member:s"
      )
    case _ => List.empty
  }
}

Test / scalacOptions ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((3, _)) =>
      List(
        // Suppress E198 unused warnings in GenericDecoderSpec - test case classes
        // used implicitly by derivation are flagged as unused in 3.3.7
        // Tracked in: https://github.com/scala/scala3/issues/24665
        "-Wconf:src=.*GenericDecoderSpec\\.scala&msg=unused local definition:s"
      )
    case _ => List.empty
  }
}

libraryDependencies ++= Libraries
libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
  case Some((3, _)) =>
    Scala3Libraries
  case _ =>
    Scala2Libraries
})
libraryDependencies ++= TestLibraries

Compile / compile / wartremoverWarnings := Warts.allBut(wartIgnoreMain: _*)
Test / compile / wartremoverWarnings := Warts.allBut(wartIgnoreTest: _*)
