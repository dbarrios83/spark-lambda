package com.endava.bd.batch

import com.endava.bd.config.Settings
import com.endava.bd.domain.Activity
import com.endava.bd.utils.SparkUtils._
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.functions._


object BatchJob {
  def main (args: Array[String]): Unit = {

    // setup spark context
    val spark = getSparkSession
    val sc = spark.sparkContext
    val sqlContext = spark.sqlContext

    import spark.implicits._

    // initialize input RDD
    val input = sc.textFile(Settings.WebLogGen.filePath)

//    input.toDF().show()

    val inputDF = input.flatMap { line =>
      val record = line.split("\\t")
      val MS_IN_HOUR = 1000 * 60 * 60
      if (record.length == 7)
        Some(Activity(record(0).toLong / MS_IN_HOUR * MS_IN_HOUR, record(1), record(2), record(3), record(4), record(5), record(6)))
      else
        None
    }.toDF()


    val df = inputDF.select(
      add_months(from_unixtime(inputDF("timestamp_hour") / 1000), 1).as("timestamp_hour"),
      inputDF("referrer"), inputDF("action"), inputDF("prevPage"), inputDF("page"), inputDF("visitor"), inputDF("product")
    ).cache()

    df.createOrReplaceTempView("activity")
    val visitorsByProduct = sqlContext.sql(
      """SELECT product, timestamp_hour, COUNT(DISTINCT visitor) as unique_visitors
        |FROM activity GROUP BY product, timestamp_hour
      """.stripMargin)

    val activityByProduct = sqlContext.sql("""SELECT
                                            product,
                                            timestamp_hour,
                                            sum(case when action = 'purchase' then 1 else 0 end) as purchase_count,
                                            sum(case when action = 'add_to_cart' then 1 else 0 end) as add_to_cart_count,
                                            sum(case when action = 'page_view' then 1 else 0 end) as page_view_count
                                            from activity
                                            group by product, timestamp_hour """).cache()

//    activityByProduct.write.partitionBy("timestamp_hour").mode(SaveMode.Append).parquet("hdfs://lambda-pluralsight:9000/lambda/batch1")

    visitorsByProduct.show()
    activityByProduct.show()

  }
}
