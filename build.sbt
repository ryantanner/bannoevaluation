name := """play-scala"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "org.scalatestplus" %% "play" % "1.1.0" % "test",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.4" % "test"
)

testOptions in Test := Seq(Tests.Argument(TestFrameworks.ScalaTest, "-oDF"))


