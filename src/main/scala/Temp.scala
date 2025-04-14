import org.apache.cassandra.io.sstable.CQLSSTableWriter
import org.apache.spark.sql.DataFrame

class Temp {

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
    val writer = CQLSSTableWriter.builder()
      .inDirectory(outputDir)
      .forTable(schema)
      .withBufferSizeInMB(256)
      .using(insertStatment)
      .build()

    writer.addRow(df)
    writer.close()
  }
}
