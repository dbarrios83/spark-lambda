package com.endava.bd

import com.endava.bd.domain.ActivityByProduct
import com.twitter.algebird.{HLL, HyperLogLogMonoid}
import org.apache.spark.streaming.State

package object functions {

  def mapActivityStateFunc = (k: (String, Long), v: Option[ActivityByProduct], state: State[(Long, Long, Long)]) => {
    var (purchase_count, add_to_cart_count, page_view_count) = state.getOption().getOrElse((0L, 0L, 0L))

    val newVal = v match {
      case Some(a: ActivityByProduct) => (a.purchase_count, a.add_to_cart_count, a.page_view_count)
      case _ => (0L, 0L, 0L)
    }

    purchase_count += newVal._1
    add_to_cart_count += newVal._2
    page_view_count += newVal._3

    state.update((purchase_count, add_to_cart_count, page_view_count))

    val underExposed = {
      if (purchase_count ==0)
        0
      else
        page_view_count / purchase_count
    }
    underExposed
  }

  def mapVisitorsStateFunc = (k: (String, Long), v: Option[HLL], state: State[HLL]) => {
    val currentVisitorHLL = state.getOption().getOrElse(new HyperLogLogMonoid(12).zero)
    val newVisitorHLL = v match {
      case Some(visitorHLL) => currentVisitorHLL + visitorHLL
      case None => currentVisitorHLL
    }
    state.update(newVisitorHLL)
    newVisitorHLL.approximateSize.estimate
  }
}
