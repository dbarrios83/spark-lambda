package com.endava.bd.config

import com.typesafe.config.ConfigFactory


object Settings {

  val config = ConfigFactory.load()

  object WebLogGen {
    private val weblogGen = config.getConfig("clickstream")
    lazy val records = weblogGen.getInt("records")
    lazy val timeMultiplier = weblogGen.getInt("time_multiplier")
    lazy val pages = weblogGen.getInt("pages")
    lazy val visitors = weblogGen.getInt("visitors")
    lazy val filePath = weblogGen.getString("file_path")
    lazy val destPath = weblogGen.getString("dest_path")
    lazy val numberOfFiles = weblogGen.getInt("number_of_files")
  }

  object kafkaSettings {
    lazy val topic = config.getString("kafka.topic")
    lazy val bootstrapServer = config.getString("kafka.bootstrap_server")
    lazy val keySerializerClass = config.getString("kafka.key_serializer_class")
    lazy val valueSerializerClass = config.getString("kafka.value_serializer_class")
    lazy val ack = config.getString("kafka.ack")
    lazy val clientId = config.getString("kafka.client_id")

  }
}
