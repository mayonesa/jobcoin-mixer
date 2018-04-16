name := "jobcoin-mixer"

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.5"

libraryDependencies ++= Seq(guice, ws,
	"net.logstash.logback" % "logstash-logback-encoder" % "5.0",
  	"org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % "test")

lazy val root = project.in(file(".")).enablePlugins(PlayScala)