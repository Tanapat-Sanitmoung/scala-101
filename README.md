# NOTE

About SSTable loader solution: I think I should drop this solution due to 
1. unhealthy dependency issue, `SLF4J: Class path contains multiple SLF4J bindings.` which is caused by adding `"org.apache.cassandra" % "cassandra-all"`
2. even I solve 1.) issue, I still have to deal with another dependency issue (Incompatible version os Json somehing which belong to `com.fasterxml.jackson.core » jackson-databind` conflict after adding `"org.apache.cassandra" % "cassandra-all"`)
3. I also found this `Stackoverflow thread` [Link](https://stackoverflow.com/questions/62450246/spark-batch-write-to-cassandra-spark-cassandra-connector-vs-sstableloader-cass) talking about `not necessary need to use sstableloader` and just use `spark-cassandra-connector` with adjust some parameter to reduce affected to `read performance`

Options:
- `spark.cassandra.output.concurrent.writes` - decrease it to 2 or 3 instead of default 5 - this will increase load time, but should decrease load to the servers
- maybe tune `spark.cassandra.output.throughputMBPerSec`, but I would suggest to start with the previous option.

4. or use `dsbluk` with some parameter to minimize affect to `read performance`

I tried to use the library from here [joswlv/Spark2CassandraBulkLoad](https://github.com/joswlv/Spark2CassandraBulkLoad).
I try to just write simplest as I can of sbt.build file as show below.
```sbt
ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.11.12"

lazy val root = (project in file("."))
  .settings(
    name := "poc"
  )

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
```
But still failed, even I try to go the Maven and click at the link but it shown as `404 Not Found`

```text
/Users/lawansiri/Library/Java/JavaVirtualMachines/corretto-1.8.0_442/Contents/Home/bin/java -Djline.terminal=jline.UnsupportedTerminal -Dsbt.log.noformat=true -Dfile.encoding=UTF-8 -Dgrouping.with.qualified.names.enabled=true -Dseparate.prod.test.sources.enabled=true -Didea.managed=true -Dfile.encoding=UTF-8 -Didea.installation.dir=/Applications/IntelliJ IDEA CE.app/Contents -jar /Users/lawansiri/Library/Application Support/JetBrains/IdeaIC2024.3/plugins/Scala/launcher/sbt-launch.jar
[info] welcome to sbt 1.10.11 (Amazon.com Inc. Java 1.8.0_442)
[info] loading project definition from /Users/lawansiri/Projects/poc/project
[info] loading settings for project root from build.sbt...
[info] set current project to poc (in build file:/Users/lawansiri/Projects/poc/)
[info] sbt server started at local:///Users/lawansiri/.sbt/1.0/server/07feae5930786a13f440/sock
[info] started sbt server
sbt:poc>
[info] Defining Global / sbtStructureOptions, Global / sbtStructureOutputFile and 1 others.
[info] The new values will be used by cleanKeepGlobs
[info] 	Run `last` for details.
[info] Reapplying settings...
[info] set current project to poc (in build file:/Users/lawansiri/Projects/poc/)
[info] Applying State transformations org.jetbrains.sbt.CreateTasks, sbt.jetbrains.LogDownloadArtifacts from /Users/lawansiri/Library/Application Support/JetBrains/IdeaIC2024.3/plugins/Scala/repo/org/jetbrains/scala/sbt-structure-extractor_2.12_1.3/2024.3.0/sbt-structure-extractor-2024.3.0.jar
[info] Reapplying settings...
[info] set current project to poc (in build file:/Users/lawansiri/Projects/poc/)
[info] Reapplying settings...
[info] set current project to poc (in build file:/Users/lawansiri/Projects/poc/)
[warn] sbt 0.13 shell syntax is deprecated; use slash syntax instead: Global / dumpStructure
[info] downloading https://repo1.maven.org/maven2/com/joswlv/spark/cassandra/bulk/Spark2CassandraBulkLoad/1.0.3/Spark2CassandraBulkLoad-1.0.3.pom
[info] downloaded https://repo1.maven.org/maven2/com/joswlv/spark/cassandra/bulk/Spark2CassandraBulkLoad/1.0.3/Spark2CassandraBulkLoad-1.0.3.pom
[info] downloading https://repo1.maven.org/maven2/com/joswlv/spark/cassandra/bulk/Spark2CassandraBulkLoad/1.0.3/Spark2CassandraBulkLoad-1.0.3.pom.sha1
[info] downloaded https://repo1.maven.org/maven2/com/joswlv/spark/cassandra/bulk/Spark2CassandraBulkLoad/1.0.3/Spark2CassandraBulkLoad-1.0.3.pom.sha1
[info] downloading https://jcenter.bintray.com/com/joswlv/spark/cassandra/bulk/Spark2CassandraBulkLoad/1.0.3/Spark2CassandraBulkLoad-1.0.3.pom
[info] downloaded https://jcenter.bintray.com/com/joswlv/spark/cassandra/bulk/Spark2CassandraBulkLoad/1.0.3/Spark2CassandraBulkLoad-1.0.3.pom
[info] downloading https://jcenter.bintray.com/com/joswlv/spark/cassandra/bulk/Spark2CassandraBulkLoad/1.0.3/Spark2CassandraBulkLoad-1.0.3.pom.sha1
[info] downloaded https://jcenter.bintray.com/com/joswlv/spark/cassandra/bulk/Spark2CassandraBulkLoad/1.0.3/Spark2CassandraBulkLoad-1.0.3.pom.sha1
[warn]
[warn] 	Note: Unresolved dependencies path:
[error] stack trace is suppressed; run 'last update' for the full output
[error] stack trace is suppressed; run 'last ssExtractDependencies' for the full output
[error] (update) sbt.librarymanagement.ResolveException: Error downloading com.joswlv.spark.cassandra.bulk:Spark2CassandraBulkLoad:1.0.3
[error]   Not found
[error]   Not found
[error]   not found: /Users/lawansiri/.ivy2/localcom.joswlv.spark.cassandra.bulk/Spark2CassandraBulkLoad/1.0.3/ivys/ivy.xml
[error]   not found: https://repo1.maven.org/maven2/com/joswlv/spark/cassandra/bulk/Spark2CassandraBulkLoad/1.0.3/Spark2CassandraBulkLoad-1.0.3.pom
[error]   not found: https://jcenter.bintray.com/com/joswlv/spark/cassandra/bulk/Spark2CassandraBulkLoad/1.0.3/Spark2CassandraBulkLoad-1.0.3.pom
[error] (ssExtractDependencies) sbt.librarymanagement.ResolveException: Error downloading com.joswlv.spark.cassandra.bulk:Spark2CassandraBulkLoad:1.0.3
[error]   Not found
[error]   Not found
[error]   not found: /Users/lawansiri/.ivy2/localcom.joswlv.spark.cassandra.bulk/Spark2CassandraBulkLoad/1.0.3/ivys/ivy.xml
[error]   not found: https://repo1.maven.org/maven2/com/joswlv/spark/cassandra/bulk/Spark2CassandraBulkLoad/1.0.3/Spark2CassandraBulkLoad-1.0.3.pom
[error]   not found: https://jcenter.bintray.com/com/joswlv/spark/cassandra/bulk/Spark2CassandraBulkLoad/1.0.3/Spark2CassandraBulkLoad-1.0.3.pom
[error] Total time: 4 s, completed Apr 18, 2025 9:51:34 PM
[info] shutting down sbt server
```