import org.apache.spark.sql.SparkSession

object App {
  def main(args: Array[String]): Unit = {
    val csvFile = "Mall_Customers.csv"

    val session = SparkSession.builder
      .appName("my-poc-app")
      .master("local[1]")
      .getOrCreate()

    val streamDf = session.read
      .option("head", value = true)
      .csv(csvFile)

    streamDf.show(numRows =  5)

    session.stop()
  }
}
