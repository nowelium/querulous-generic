package com.twitter.querulous
package database

import java.sql.Connection
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
      config("min_evictable_idle_msec").toLong.millis)
    statsCollector.foreach { stats =>
      factory = new StatsCollectingDatabaseFactory(factory, stats)
    }
    config.getConfigMap("timeout").foreach { timeoutConfig =>
      factory = new TimingOutDatabaseFactory(factory,
        timeoutConfig("pool_size").toInt,
        timeoutConfig("queue_size").toInt,
        timeoutConfig("open").toLong.millis,
        timeoutConfig("initialize").toLong.millis,
        config("size_max").toInt)
    }
    new MemoizingDatabaseFactory(factory)
  }
}

trait DatabaseFactory {
  def apply(jdbcDriver: String, jdbcUrl: String, username: String, password: String): Database
}

trait Database {
  def open(): Connection

  def close(connection: Connection)

  def withConnection[A](f: Connection => A): A = {
    val connection = open()
    try {
      f(connection)
    } finally {
      close(connection)
    }
  }
}
