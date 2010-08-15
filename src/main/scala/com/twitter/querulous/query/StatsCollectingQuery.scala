package com.twitter.querulous.query

import com.twitter.querulous.StatsCollector 

import java.sql.{ResultSet, Connection}

class StatsCollectingQuery(query: Query, stats: StatsCollector) extends QueryProxy(query) {
  override def select[A](f: ResultSet => A) = {
    stats.incr("db-select-count", 1)
    delegate(query.select(f))
  }

  override def execute() = {
    stats.incr("db-execute-count", 1)
    delegate(query.execute())
  }

  override def call[A](f: ResultSet => A) = {
    stats.incr("db-procedure-count", 1)
    delegate(query.call(f))
  }

  override def delegate[A](f: => A) = {
    stats.time("db-timing")(f)
  }
}
