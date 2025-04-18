import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.cassandra.DataFrameWriterWrapper
import org.apache.spark.sql.types._

import scala.io.Source
import upickle.default._

import scala.annotation.tailrec
import scala.sys.exit

object App {
  def main(args: Array[String]): Unit = {

    // Read command line arguments
    val options = mapArgs(Map(), args.toList)

    // print command line arguments
    println("Input options:")
    println(options)

    // Set spark config
    val conf = new SparkConf()
      .setMaster("local[1]")
      .set("spark.eventLog.enabled", "true")
      .set("spark.eventLog.dir", "./logs")
      .set("spark.files.maxPartitionBytes", getBytesString(megaBytes =  128))
      // .set("spark.cassandra.connection.config.cloud.path", "hdfs:///some_dir/bundle.zip")
      // .set("spark.cassandra.auth.username", "")
      // .set("spark.cassandra.auth.password", "")
      .set("spark.cassandra.connection.host", "localhost")
      .set("spark.cassandra.connection.port", "9042")
      // reference to spark.cassandra configurations
      // https://github.com/apache/cassandra-spark-connector/blob/trunk/doc/reference.md
      // see : Write Tuning Parameters section
      .set("spark.cassandra.output.batch.grouping.buffer.size", "1000")
      .set("spark.cassandra.output.batch.size.bytes", "1024")
      .set("spark.cassandra.output.concurrent.writes", "5")
      .set("spark.cassandra.output.ifNotExists", "false")
      .set("spark.cassandra.output.metrics", "true")
      .set("spark.cassandra.output.throughputMBPerSec", "None")
      .set("spark.cassandra.output.ttl", "0")
      .setAppName("my-poc-app")

    // Create Spark Session
    val session = SparkSession.builder
      .config(conf)
      .getOrCreate()

    val allCfg = session.sparkContext.getConf.getAll

    // Get required parameter for specify csv file
    val csvConfig = getCsvConfig(
      options.get("--csv-config") match {
        case Some(v) => v
        case None =>
          println("--schema-file is required")
          exit(1)
    })

    // Load schema
    val csvSchema = getCsvSchema(csvConfig.mappings)

    println("Csv Schema:")
    println(csvSchema)

    // Read CSV file
    val df = session.read
      .option("header", value = csvConfig.has_header)
      .schema(csvSchema)
      .csv(csvConfig.file_name)
      .repartition(csvConfig.num_partition)

    // show sample rows
    df.show(numRows =  5)

    // show number of partition
    val numPartition = df.rdd.getNumPartitions
    println(s"Number of partition := $numPartition")

    // write data to database
    val notDryRun = options.get("--mode") match {
      case Some(x) if x == "dry-run" => false
      case None => true
    }

    if (notDryRun) {
      println("write data to cassandra")
      df.write
        .cassandraFormat(
          table = csvConfig.to_cass_table,
          keyspace = csvConfig.to_cass_keyspace)
        .save()
      println("done write data to cassandra")
    }

    session.stop()
    println("Session stop")
  }

  @tailrec
  private def mapArgs(map: Map[String, String], list: List[String]) : Map[String, String]
  = {
    list match {
      case Nil => map
      case (cmd @ ("--csv-config" | "--mode"))
        :: value :: tail => mapArgs(map ++ Map(cmd -> value), tail)
      case unknown :: _ =>
        println(s"Unknown command argument := $unknown")
        exit(1)
    }
  }

  def getBytesString(megaBytes: Int): String = (megaBytes * 1048576).toString


  case class MapField(name: String, dataType: String)
  case class CsvConfig(
                        file_name: String,
                        has_header: Boolean,
                        mappings: Seq[MapField],
                        to_cass_table: String,
                        to_cass_keyspace: String,
                        num_partition: Integer)

  implicit val seqFieldsRw: ReadWriter[MapField] = macroRW
  implicit val schemaRw: ReadWriter[CsvConfig] = macroRW

  private def getCsvConfig(filePath: String): CsvConfig = {
    val src = Source.fromFile(filePath)
    val cfg = read[CsvConfig](src.mkString)
    src.close()
    cfg
  }

  private def getCsvSchema(mappings: Seq[MapField]): StructType = {
    val structType = StructType(
        mappings.map(f => StructField(f.name, f.dataType match {
        case "IntegerType" => IntegerType
        case "BooleanType" => BooleanType
        case "DateType" => DateType
        case "DoubleType" => DoubleType
        case _  => StringType
        // Add more
      }, nullable =  false))
    )
    structType
  }

}