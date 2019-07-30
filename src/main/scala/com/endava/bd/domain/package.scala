package com.endava.bd

/**
  * Created by Ahmad Alkilani on 5/1/2016.
  */
package object domain {
  case class Activity(timestamp_hour: Long,
                      referrer: String,
                      action: String,
                      prevPage: String,
                      visitor: String,
                      page: String,
                      product: String,
                      inputProps: Map[String, String] = Map()
                     )
}
