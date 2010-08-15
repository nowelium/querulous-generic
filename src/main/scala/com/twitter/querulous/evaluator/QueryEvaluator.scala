package com.twitter.querulous.evaluator

import com.twitter.querulous.StatsCollector
import java.sql.ResultSet
import com.twitter.xrayspecs.TimeConversions._
import net.lag.configgy.ConfigMap
import com.twitter.querulous.database._
import com.twitter.querulous.query._

object QueryEvaluator extends QueryEvaluatorFactory {
  private def createEvaluatorFactory() = {
    val queryFactory = new SqlQueryFactory
    val databaseFactory = new ApachePoolingDatabaseFactory(null, 10, 10, 1.second, 10.millis, false, 0.seconds)
    new StandardQueryEvaluatorFactory(databaseFactory, queryFactory)
  }

  def apply(jdbcDriver: String, jdbcUrl: String, username: String, password: String) = {
    createEvaluatorFactory()(jdbcDriver, jdbcUrl, username, password)
  }

  def apply(config: ConfigMap) = {
    createEvaluatorFactory()(config("hostname"), config("database"), config("username"), config("password"))
  }
}

trait QueryEvaluator {
  def select[A](query: String, params: Any*)(f: ResultSet => A): Seq[A]
  def selectOne[A](query: String, params: Any*)(f: ResultSet => A): Option[A]
  def count(query: String, params: Any*): Int
  def execute(query: String, params: Any*): Int
  def call[A](query: String, params: Any*)(f: ResultSet => A): Seq[A]
  def transaction[T](f: Transaction => T): T
}
