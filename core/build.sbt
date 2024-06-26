import Dependencies._
import Wartremover._

name := "medeia"

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