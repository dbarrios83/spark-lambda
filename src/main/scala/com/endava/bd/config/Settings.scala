package com.endava.bd.config

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SparkSession

/**
  * Created by Ahmad Alkilani on 4/30/2016.
  */
object Settings {

  private val config = ConfigFactory.load()

  lazy val appName: String = config.getString("app.name")

  lazy val getSpark: SparkSession = {
    SparkSession
      .builder
      .appName(appName)
      .master("local[*]")
      .config("spark.sql.streaming.checkpointLocation", config.getString("app.checkpoint_path"))
      .getOrCreate()
  }

  object WebLogGen {
    private val weblogGen = config.getConfig("clickstream")

    lazy val records = weblogGen.getInt("records")
    lazy val timeMultiplier = weblogGen.getInt("time_multiplier")
    lazy val pages = weblogGen.getInt("pages")
    lazy val visitors = weblogGen.getInt("visitors")
    lazy val filePath = weblogGen.getString("file_path")

  }
}
