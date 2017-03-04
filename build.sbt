lazy val root = (project in file("."))
  .settings(
    name         := "family",
    organization := "info.teksol",
    version      := "0.1.0-SNAPSHOT",
    scalaVersion := "2.12.1"
  )

javacOptions ++= Seq("-source", "1.8")

scalacOptions += "-target:jvm-1.8"
