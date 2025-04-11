import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._

object App {
  def main(args: Array[String]): Unit = {

    val session = SparkSession.builder
      .appName("my-poc-app")
      .master("local[1]")
      .getOrCreate()

    val csvFile = "Mall_Customers.csv"

    val mallCustomerSchema = new StructType()
      .add("customerid", IntegerType)
      .add("genre", StringType)
      .add("annual_income_k", IntegerType)
      .add("spending_score", IntegerType)

    val streamDf = session.read
      .option("head", value = true)
      .schema(mallCustomerSchema)
      .csv(csvFile)

    streamDf.show(numRows =  5)

    session.stop()
  }
}
