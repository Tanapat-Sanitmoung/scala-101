ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.11.12"

lazy val root = (project in file("."))
  .settings(
    name := "poc"
  )

//libraryDependencies ++= {
//
//  val sparkV = "2.1.0"
//  val cassandraV = "2.0.0-M3"
//
//  Seq(
//    "org.apache.spark"      %% "spark-core" % sparkV,
//    "org.apache.spark"      %% "spark-streaming" % sparkV,
//    "org.apache.spark"      %% "spark-sql" % sparkV,
//    "org.apache.cassandra" % "cassandra-all"  % "3.0.32"
//  )
//
//}

resolvers += Resolver.jcenterRepo

libraryDependencies ++= {

  val sparkV = "2.4.8"

  Seq(
    "org.apache.spark"      %% "spark-core" % sparkV,
    "org.apache.spark"      %% "spark-sql" % sparkV,
    // this library is not exist anymore on jcenterRepo
    "com.joswlv.spark.cassandra.bulk" %  "Spark2CassandraBulkLoad" % "1.0.3"
  )
}