// // See README.md for license details.

// ThisBuild / scalaVersion     := "2.13.12"
// ThisBuild / version          := "0.1.0"
// ThisBuild / organization     := "ucb-eecs251b"

// val chiselVersion = "6.6.0"

// lazy val root = (project in file("."))
//   .settings(
//     name := "gmii_rx_tx",
//     libraryDependencies ++= Seq(
//       "org.chipsalliance" %% "chisel" % chiselVersion,
//       "org.scalatest" %% "scalatest" % "3.2.16" % "test",
//     ),
//     scalacOptions ++= Seq(
//       "-language:reflectiveCalls",
//       "-deprecation",
//       "-feature",
//       "-Xcheckinit",
//       "-Ymacro-annotations",
//     ),
//     addCompilerPlugin("org.chipsalliance" % "chisel-plugin" % chiselVersion cross CrossVersion.full),
//   )