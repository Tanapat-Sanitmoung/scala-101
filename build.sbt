ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.20"

lazy val root = (project in file("."))
  .settings(
    name := "poc"
  )


val sparkVersion = "2.4.8"

// Exclude conflicting SLF4J bindings
libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion exclude("org.slf4j", "slf4j-log4j12")
libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion exclude("org.slf4j", "slf4j-log4j12")
libraryDependencies += "org.apache.spark" %% "spark-streaming" % sparkVersion exclude("org.slf4j", "slf4j-log4j12")
//libraryDependencies += "com.datastax.spark" %% "spark-cassandra-connector" % "2.4.3" exclude("ch.qos.logback", "logback-classic")
//
//// https://mvnrepository.com/artifact/com.lihaoyi/upickle
//libraryDependencies += "com.lihaoyi" %% "upickle" % "4.1.0"
// https://mvnrepository.com/artifact/org.apache.cassandra/cassandra-all
libraryDependencies += "org.apache.cassandra" % "cassandra-all" % "3.11.19" exclude("com.fasterxml.jackson.core", "jackson-databind")

