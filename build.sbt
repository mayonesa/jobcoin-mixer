name := "jobcoin-mixer"

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.5"

libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "5.0"

lazy val root = project.in(file(".")).enablePlugins(PlayScala)