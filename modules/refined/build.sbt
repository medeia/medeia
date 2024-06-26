import Dependencies._
import Wartremover._

name := "medeia-refined"

libraryDependencies ++= Libraries
libraryDependencies ++= RefinedLibraries
libraryDependencies ++= TestLibraries
libraryDependencies ++= RefinedTestLibraries

Compile / compile / wartremoverWarnings := Warts.allBut(wartIgnoreMain: _*)
Test / compile / wartremoverWarnings := Warts.allBut(wartIgnoreTest: _*)