# NOTE

1. run `podman compose -f docker-compose.yml -p poc up -d`. to populate dse-node (Cassandra). ** change image Tag to match with your machine
2. execute `create-table.cql` by which method is good for you.
3. run `App`

## LINKS
- [Scala Docs](https://archive.apache.org/dist/spark/docs/2.4.0/api/scala/index.html)
- [Spark Docs](https://archive.apache.org/dist/spark/docs/2.4.0/sql-data-sources.html)
- [DataFrame API](https://docs.datastax.com/en/dse/6.9/spark/data-frames.html)
## OUTPUT

Insert successfully
```text
dse@b060f8f39051:~$ dse@b060f8f39051:~$ cqlsh
Connected to Test Cluster at 127.0.0.1:9042.
[cqlsh 6.8.0 | DSE 6.8.37 | CQL spec 3.4.5 | DSE protocol v2]
Use HELP for help.
cqlsh> use poc;
cqlsh:poc> select count(*) from customer_data
       ... ;

 count
-------
     0

(1 rows)

Warnings :
Aggregation query used without partition key

cqlsh:poc> select count(*) from customer_data ;

 count
-------
   200

(1 rows)

Warnings :
Aggregation query used without partition key

cqlsh:poc> 
```