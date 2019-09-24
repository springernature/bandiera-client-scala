
name := "bandiera-client-scala"
version := "0.3.0"
organization := "com.springernature"
description :=
  """
    | scala client for bandiera feature flags service.
    | communicate with the rest api of a bandiera server.
    | only GET operations are implemented.
    |""".stripMargin
licenses := Seq("MIT" -> url("https://github.com/springernature/bandiera-client-scala/blob/master/LICENSE"))

scalaVersion := "2.12.10"
// publish for 2.11, 2.12 and 2.13
// no need to have multiple versions of same major (aka 2.13.0 and 2.13.1)
// because of binary compat and because the minor version gets stripped out
// and the final published artifact just contains major version: 2.12, 2.13...
crossScalaVersions := List("2.11.12", "2.12.10", "2.13.1")

libraryDependencies ++= List(
  "com.softwaremill.sttp" %% "core" % "1.6.0+",
  "com.softwaremill.sttp" %% "async-http-client-backend-future" % "1.6.0+",
  "com.lihaoyi" %% "upickle" % "0+",
  "com.lihaoyi" %% "utest" % "0.6+" % Test
)
testFrameworks := Seq(new TestFramework("com.springernature.bandieraclientscala.tests.CustomFramework"))


// gpg signing credentials
// - using https://github.com/sbt/sbt-pgp/
// - uses gpg-agent under the hood
credentials += Credentials(
  "GnuPG Key ID",
  "gpg",
  "3427CA7B4A759D2AFE2E1025E8D4EACC424C5928", // key identifier
  "ignored" // this field is ignored; passwords are supplied by pinentry
)


// published to sonatype
// `+publishSigned` to cross publish
// credentials to sonatype are in /Users/user/.sbt/1.0/sonatype.sbt
// and look like
// credentials += Credentials("Sonatype Nexus Repository Manager",
//                           "oss.sonatype.org",
//                           "username",
//                           "urpasshere")
// after publishing goto
// https://oss.sonatype.org/
// login
// goto staging repositories
// refresh list
// scroll down and look for your package "comsprinagernature"
// then
publishMavenStyle := true
pomIncludeRepository := { _ => false }

homepage := Some(url("https://github.com/springernature/bandiera-client-scala"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/springernature/bandiera-client-scala"),
    "scm:git@github.com:springernature/bandiera-client-scala.git"
  )
)
developers := List(
  Developer(
    id = "samzilverberg",
    name = "Samuel Zilverberg",
    email = "samuel.zilverberg@springernature.com",
    url = url("http://github.com/samzilverberg")
  )
)

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false
