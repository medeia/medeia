lazy val root = (project in file("."))
  .settings(name := "medeia", organization := "de.megaera")
  .settings(Dependencies.Libraries, Dependencies.TestLibraries)
  .enablePlugins(MiscSettingsPlugin)
