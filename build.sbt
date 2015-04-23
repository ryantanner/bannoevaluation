name := """banno-eval"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "com.twitter" %% "algebird-core" % "0.9.0",
  "com.vdurmont" % "emoji-java" % "1.1.0",
  "org.scalatestplus" %% "play" % "1.1.0" % "test",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.4" % "test"
)

testOptions in Test := Seq(Tests.Argument(TestFrameworks.ScalaTest, "-oDF"))


