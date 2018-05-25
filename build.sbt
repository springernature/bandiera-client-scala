lazy val root = (project in file(".")).
  settings(
    useGpg := true,
    inThisBuild(List(
      organization := "com.springernature",
      scalaVersion := "2.12.6",
      version      := "0.1.0"
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

// publish to sonatype

useGpg := true
pomIncludeRepository := { _ => false }
licenses := Seq("MIT" -> url("https://github.com/springernature/bandiera-client-scala/blob/master/LICENSE"))
homepage := Some(url("https://github.com/springernature/bandiera-client-scala"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/springernature/bandiera-client-scala"),
    "scm:git@github.com:springernature/bandiera-client-scala.git"
  )
)

publishMavenStyle := true


developers := List(
  Developer(
    id    = "samzilverberg",
    name  = "Samuel Zilverberg",
    email = "samuel.zilverberg@springernature.com",
    url   = url("http://github.com/samzilverberg")
  )
)

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false
