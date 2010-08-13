package com.twitter.querulous.database

import com.twitter.querulous.StatsCollector

import com.twitter.xrayspecs.TimeConversions._
import net.lag.configgy.ConfigMap

object DatabaseFactory {
  def fromConfig(config: ConfigMap, statsCollector: Option[StatsCollector]) = {
    var factory: DatabaseFactory = new ApachePoolingDatabaseFactory(
      config("validation_query"),
      config("size_min").toInt,
      config("size_max").toInt,
      config("test_idle_msec").toLong.millis,
      config("max_wait").toLong.millis,
      config("test_on_borrow").toBoolean,
      config("min_evictable_idle_msec").toLong.millis
    )
    statsCollector.foreach { stats =>
      factory = new StatsCollectingDatabaseFactory(factory, stats)
    }
    config.getConfigMap("timeout").foreach { timeoutConfig =>
      factory = new TimingOutDatabaseFactory(
        factory,
        timeoutConfig("pool_size").toInt,
        timeoutConfig("queue_size").toInt,
        timeoutConfig("open").toLong.millis,
        timeoutConfig("initialize").toLong.millis,
        config("size_max").toInt
      )
    }
    new MemoizingDatabaseFactory(factory)
  }
}

trait DatabaseFactory {
  def apply(jdbcDriver: String, jdbcUrl: String, username: String, password: String): Database
}

