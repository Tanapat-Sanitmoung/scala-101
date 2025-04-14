import org.apache.cassandra.io.sstable.CQLSSTableWriter
import org.apache.spark.SparkConf
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
//import upickle.default._

import scala.annotation.tailrec
import scala.io.Source
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
      .setMaster(options.getOrElse("--spark-master", default = "local[1]"))
      .setAppName("my-poc-app")

    val session = SparkSession.builder
      .config(conf)
      .getOrCreate()

    val csvFile = options.get("--csv-file") match {
      case Some(x) => x
      case None =>
        println("--csv-file is required")
        exit(1)
    }

    val schema = new StructType()
      .add("customerid", IntegerType)
      .add("genre", StringType)
      .add("age", IntegerType)
      .add("annual_income_k", IntegerType)
      .add("spending_score", IntegerType)

    val df = session.read
      .option("header", value = options.get("--contains-header").exists(a => a.toBoolean))
      .schema(schema)
      .csv(csvFile)

    df.show(numRows =  5)

    val partition1 = df.rdd.getNumPartitions
    println(s"Number of partition := $partition1")

    writeSSTable(df)

    session.stop()
  }

  private def writeSSTable(df: DataFrame): Unit = {
    val outputDir = "./sstables"
    val schema =
      """
        |CREATE TABLE poc.customer_data (
        |    customerid INT PRIMARY KEY,
        |    genre TEXT,
        |    age INT,
        |    annual_income_k INT,
        |    spending_score INT
        |);
      """.stripMargin
    val insertStatment = "INSERT INTO poc.customer_data (customerid, genre, age, annual_income_k, spending_score) VALUES (?, ?, ?, ?, ?);"

    df.foreachPartition { partition: Iterator[Row] =>

      val writer = CQLSSTableWriter.builder()
        .inDirectory(outputDir)
        .forTable(schema)
        .withBufferSizeInMB(256)
        .using(insertStatment)
        .build()

      partition.foreach { r =>
        writer.addRow(
          r.getAs[Integer]("customerid"),
          r.getAs[String]("genre"),
          r.getAs[Integer]("age"),
          r.getAs[Integer]("annual_income_k"),
          r.getAs[Integer]("spending_score"))
      }

      writer.close()

    }
  }
}
