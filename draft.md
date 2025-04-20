
Hi, team.

I just inform that 

1. I think I will abandon the SSTable solution due to dependency conflict issue between Spark and Apache Cassandra and I can't solve the issue and it seem like no one use this solution for long time
2. I will go with a solution that use `spark-cassandra-connector` from Datastax which I have done it successfully on my machine

about `spark-cassandra-connector`
1. I need to test in dev environment and adjust some parameter (spark configuration and csv configurations)
2. As we discuss using the `readStream` method of SparkSession, to read it to buffer and then send to cassandra once buffer hit threshold. 
as far as I understand, it doesn't work as we expected.
the method design to monitor changes of `directory` or `table` for certain conditions and the process and flush data to external data store.
3. for now, I'm using `read` method of SparkSession. it will read a file and then create partitions which will be 1 task per 1 partition.
and Spark will send tasks across its worker nodes.
for example:
If BBL export a file of 2 GB size.
When Spark read it, I will create 128 MB (by default) for 16 files (1 file / 1 partition)
and then if we have 2 workers, each worker will process 8 files. we need to find best fit size of partition to our environment.
4. We also need to adjust some parameters of `spark-cassandra-connector`.  
For example:  
`spark.cassandra.output.concurrent.writes` default value is `5`,   
`spark.cassandra.output.throughputMBPerSec` default value is `No Limit` << this should be limit to reduce read latency issue,   
`spark.cassandra.output.batch.size.bytes` default value is `1024` bytes,   
`spark.cassandra.output.batch.grouping.buffer.size`  default value is `1000` bytes  
and others if needed.
5. We also need to adjust another parameter if I read more from the internet.