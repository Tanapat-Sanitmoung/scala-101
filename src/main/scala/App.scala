import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.cassandra.DataFrameWriterWrapper
import org.apache.spark.sql.types._

object App {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .set("spark.cassandra.connection.host", "localhost")
      .set("spark.cassandra.connection.port", "9042")
      .setMaster("local[1]")
      .setAppName("my-poc-app")

    val session = SparkSession.builder
      .config(conf)
      .getOrCreate()

    val csvFile = "Mall_Customers.csv"

    val mallCustomerSchema = new StructType()
      .add("customerid", IntegerType)
      .add("genre", StringType)
      .add("annual_income_k", IntegerType)
      .add("spending_score", IntegerType)

    val streamDf = session.read
      .option("header", value = true)
      .schema(mallCustomerSchema)
      .csv(csvFile)

    streamDf.show(numRows =  5)

    streamDf.write
      .cassandraFormat(table = "customer_data", keyspace = "poc")
      .save()

    session.stop()
  }
}
