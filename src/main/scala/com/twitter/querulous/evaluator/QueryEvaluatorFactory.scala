package com.twitter.querulous.evaluator

import java.sql.ResultSet
import net.lag.configgy.ConfigMap
import com.twitter.xrayspecs.TimeConversions._
import com.twitter.querulous.StatsCollector
import com.twitter.querulous.database._
import com.twitter.querulous.query._

object QueryEvaluatorFactory {
  def fromConfig(config: ConfigMap, databaseFactory: DatabaseFactory, queryFactory: QueryFactory): QueryEvaluatorFactory = {
    var factory: QueryEvaluatorFactory = new StandardQueryEvaluatorFactory(databaseFactory, queryFactory)
    config.getConfigMap("disable").foreach { disableConfig =>
      factory = new AutoDisablingQueryEvaluatorFactory(
        factory,
        disableConfig("error_count").toInt,
        disableConfig("seconds").toInt.seconds
      )
    }
    factory
  }

  def fromConfig(config: ConfigMap, statsCollector: Option[StatsCollector]): QueryEvaluatorFactory = {
    fromConfig(config,
      DatabaseFactory.fromConfig(config.configMap("connection_pool"), statsCollector),
      QueryFactory.fromConfig(config, statsCollector)
    )
  }
}

trait QueryEvaluatorFactory {
  def apply(jdbcDriver: String, jdbcUrl: String, username: String, password: String): QueryEvaluator
}
