package com.twitter.querulous.query

import com.twitter.querulous.StatsCollector  
import java.sql.{Connection, ResultSet}
import scala.collection.Map
import scala.util.matching.Regex
import scala.collection.Map
import com.twitter.xrayspecs.Duration
import net.lag.extensions._

class TimingOutStatsCollectingQuery(query: Query, queryName: String, stats: StatsCollector) extends QueryProxy(query) {
  override def select[A](f: ResultSet => A) = {
    stats.incr("db-select-count", 1)
    stats.time("db-select-timing")(delegate(query.select(f)))
  }

  override def execute() = {
    stats.incr("db-execute-count", 1)
    stats.time("db-execute-timing")(delegate(query.execute()))
  }

  override def call[A](f: ResultSet => A) = {
    stats.incr("db-procedure-count", 1)
    stats.time("db-procedure-timing")(delegate(query.call(f)))
  }

  override def delegate[A](f: => A) = {
    stats.incr("db-query-count-" + queryName, 1)
    stats.time("db-timing") {
      stats.time("x-db-query-timing-" + queryName) {
        f
      }
    }
  }
}
