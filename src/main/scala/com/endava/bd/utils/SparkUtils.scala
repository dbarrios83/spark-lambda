package com.endava.bd.utils

import com.endava.bd.config.Settings
import com.endava.bd.config.Settings.config
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Duration, Seconds, StreamingContext}
import org.apache.spark.SparkContext


object SparkUtils {


  lazy val appName: String = Settings.config.getString("app.name")
  lazy val checkPointPath: String = Settings.config.getString("app.name")
  lazy val batchDuration: Duration = Seconds(Settings.config.getLong("app.batch_interval"))

  lazy val getSparkSession: SparkSession = {
    SparkSession
      .builder
      .appName(appName)
      .master("local[*]")
      .config("spark.sql.streaming.checkpointLocation", config.getString("app.checkpoint_path"))
      .getOrCreate()
  }

  def getStreamingContext(streamingApp: (SparkContext, Duration) => StreamingContext, sc: SparkContext, batchDuration: Duration) = {
    val creatingFunc = () => streamingApp(sc, batchDuration)
    val ssc = sc.getCheckpointDir match {
      case Some(checkpointDir) => StreamingContext.getActiveOrCreate(checkpointDir, creatingFunc, sc.hadoopConfiguration, createOnError = true)
      case None => StreamingContext.getActiveOrCreate(creatingFunc)
    }
    sc.getCheckpointDir.foreach(cp => ssc.checkpoint(cp))
    ssc.checkpoint(config.getString("app.checkpoint_path"))
    ssc
  }

}
