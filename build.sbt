
lazy val root = (project in file(".")).
  settings(

    inThisBuild(List(
      organization := "com.springernature",
      scalaVersion := "2.12.6",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "bandiera-client-scala",
    libraryDependencies ++= List(
      "com.softwaremill.sttp" %% "core" % "1.1.14",
      "com.softwaremill.sttp" %% "async-http-client-backend-future" % "1.1.14",
      "com.typesafe.play" %% "play-json" % "2.6.8",
      "com.lihaoyi" %% "utest" % "0.6.3" % "test"
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),
    parallelExecution in Test := false
  )


