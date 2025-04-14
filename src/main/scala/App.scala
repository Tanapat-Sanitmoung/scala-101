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

    @tailrec
    def mapArgs(map: Map[String, String], list: List[String]) : Map[String, String]
    = {
      list match {
        case Nil => map
        case (cmd @ ("--csv-file" | "--schema-file" | "--contains-header" | "--spark-master"
          | "--cassandra-host-ip" | "--cassandra-host-port"))
          :: value :: tail => mapArgs(map ++ Map(cmd -> value), tail)
        case unknown :: _ =>
          println(s"Unknown command argument := $unknown")
          exit(1)
      }
    }

    val options = mapArgs(Map(), args.toList)

    // log
    println("Input options:")
    print(options)

    val conf = new SparkConf()
      .set("spark.cassandra.connection.host", options.getOrElse("--cassandra-host-ip", default = "localhost"))
      .set("spark.cassandra.connection.port", options.getOrElse("--cassandra-host-port", default = "9042"))
      .setMaster(options.getOrElse("--spark-master", default = "local[2]"))
      .setAppName("my-poc-app")

    val session = SparkSession.builder
      .config(conf)
      .getOrCreate()

    val schema = loadSchemaFromJson(
      options.get("--schema-file") match {
        case Some(v) => v
        case None =>
          println("--schema-file is required")
          exit(1)
      }
    )

    println("Schema:")
    println(schema)

    val csvFile = options.get("--csv-file") match {
      case Some(x) => x
      case None =>
        println("--csv-file is required")
        exit(1)
    }
    val df = session.read
      .option("header", value = options.get("--contains-header").exists(a => a.toBoolean))
      .schema(schema)
      .csv(csvFile)

    df.show(numRows =  5)

    val partition1 = df.rdd.getNumPartitions
    println(s"Number of partition := $partition1")

    df.write
      .cassandraFormat(table = "customer_data", keyspace = "poc")
      .save()

    session.stop()
  }

  case class Field(name: String, dataType: String)
  case class Schema(structure_type: Seq[Field])
  implicit val seqFieldsRw: ReadWriter[Field] = macroRW
  implicit val schemaRw: ReadWriter[Schema] = macroRW

  private def loadSchemaFromJson(filePath: String): StructType = {

    // Read and parse the JSON file
    val src = Source.fromFile(filePath)
    val schema = read[Schema](src.mkString)
    src.close()

    val structType = StructType(
      schema.structure_type.map(f => StructField(f.name, f.dataType match {
        case "IntegerType" => IntegerType
        case _  => StringType
        // Add more
      }, nullable =  false))
    )
    structType
  }

}
