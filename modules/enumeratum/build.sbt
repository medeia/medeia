import Dependencies._
import Wartremover._

name := "medeia-enumeratum"

scalacOptions ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((3, n)) => List("-Yretain-trees")
    case _            => List.empty
  }
}

libraryDependencies ++= Libraries
libraryDependencies ++= EnumeratumLibraries
libraryDependencies ++= TestLibraries
libraryDependencies ++= EnumeratumTestLibraries

Compile / compile / wartremoverWarnings := Warts.allBut(wartIgnoreMain: _*)
Test / compile / wartremoverWarnings := Warts.allBut(wartIgnoreTest: _*)
