import Dependencies._

organization := "de.megaera"
name := "medeia-enumeratum"

libraryDependencies ++= Libraries
libraryDependencies ++= EnumeratumLibraries
libraryDependencies ++= TestLibraries
libraryDependencies ++= EnumeratumTestLibraries

// disable module for scala 3.X
libraryDependencies := {
  if (scalaBinaryVersion.value == "3") {
    Nil
  } else {
    libraryDependencies.value
  }
}

Seq(Compile, Test).map { x =>
  (x / sources) := {
    if (scalaBinaryVersion.value == "3") {
      Nil
    } else {
      (x / sources).value
    }
  }
}

Test / test := {
  if (scalaBinaryVersion.value == "3") {
    ()
  } else {
    (Test / test).value
  }
}

publish / skip := (scalaBinaryVersion.value == "3")
