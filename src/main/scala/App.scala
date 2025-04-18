import org.apache.cassandra.io.sstable.CQLSSTableWriter
import org.apache.spark.SparkConf
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

object App {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setMaster("local[1]")
      .setAppName("my-poc-app")

    val session = SparkSession.builder
      .config(conf)
      .getOrCreate()

    val csvFile = "Mall_Customers.csv"

    val schema = new StructType()
      .add("customerid", IntegerType)
      .add("genre", StringType)
      .add("age", IntegerType)
      .add("annual_income_k", IntegerType)
      .add("spending_score", IntegerType)

    val df = session.read
      .option("header", value = true)
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
