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
        case (cmd @ ("--csv-file" | "--csv-config"))
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
      .setMaster("local[1]")
      .setAppName("my-poc-app")

    val session = SparkSession.builder
      .config(conf)
      .getOrCreate()

    val csvConfig = getCsvConfig(
      options.get("--csv-config") match {
        case Some(v) => v
        case None =>
          println("--schema-file is required")
          exit(1)
    })

    val csvSchema = getCsvSchema(csvConfig.mappings)

    println("Csv Schema:")
    println(csvSchema)

    val csvFile = options.get("--csv-file") match {
      case Some(x) => x
      case None =>
        println("--csv-file is required")
        exit(1)
    }

    val df = session.read
      .option("header", value = csvConfig.has_header)
      .schema(csvSchema)
      .csv(csvFile)
      .repartition(csvConfig.num_partition)

    // show Example row
    df.show(numRows =  5)

    val numPartition = df.rdd.getNumPartitions
    println(s"Number of partition := $numPartition")

    df.write
      .cassandraFormat(
        table = csvConfig.to_cass_table,
        keyspace = csvConfig.to_cass_keyspace)
      .save()

    session.stop()
  }

  case class MapField(name: String, dataType: String)
  case class CsvConfig(
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
