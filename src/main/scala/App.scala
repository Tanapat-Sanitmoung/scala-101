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

    val df = session.read
      .option("header", value = true)
      .schema(mallCustomerSchema)
      .csv(csvFile)

    df.show(numRows =  5)

    val partition1 = df.rdd.getNumPartitions.toInt
    df.rdd.repartition(2)
    val partition2 = df.rdd.getNumPartitions.toInt
    println(s"P1 = $partition1, P2 = $partition2")
    // P1 = 1, P2 = 1

    df.write
      .cassandraFormat(table = "customer_data", keyspace = "poc")
      .save()

    session.stop()
  }
}
