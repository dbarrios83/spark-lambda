package com.endava.bd.utils

import java.lang.management.ManagementFactory

import com.endava.bd.config.Settings
import com.endava.bd.config.Settings.config
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.apache.spark.streaming.{Duration, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}


object SparkUtils {


  lazy val appName: String = Settings.config.getString("app.name")
  lazy val checkPointPath: String = Settings.config.getString("app.name")

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
    sc.getCheckpointDir.foreach( cp => ssc.checkpoint(cp))
    ssc
  }

}
