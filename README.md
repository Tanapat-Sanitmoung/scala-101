# NOTE

About SSTable loader solution: I think I should drop this solution due to 
1. unhealthy dependency issue, `SLF4J: Class path contains multiple SLF4J bindings.` which is caused by adding `"org.apache.cassandra" % "cassandra-all"`
2. even I solve 1.) issue, I still have to deal with another dependency issue (Incompatible version os Json somehing which belong to `com.fasterxml.jackson.core » jackson-databind` conflict after adding `"org.apache.cassandra" % "cassandra-all"`)
3. I also found this `Stackoverflow thread` [Link](https://stackoverflow.com/questions/62450246/spark-batch-write-to-cassandra-spark-cassandra-connector-vs-sstableloader-cass) talking about `not necessary need to use sstableloader` and just use `spark-cassandra-connector` with adjust some parameter to reduce affected to `read performance`

Options:
- `spark.cassandra.output.concurrent.writes` - decrease it to 2 or 3 instead of default 5 - this will increase load time, but should decrease load to the servers
- maybe tune `spark.cassandra.output.throughputMBPerSec`, but I would suggest to start with the previous option.

4. or use `dsbluk` with some parameter to minimize affect to `read performance`