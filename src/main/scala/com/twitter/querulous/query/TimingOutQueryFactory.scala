package com.twitter.querulous.query

import java.util.concurrent.TimeoutException
import java.sql.{SQLException, Connection}
import com.twitter.xrayspecs.Duration
import com.twitter.querulous.Timeout
import com.twitter.querulous.StatsCollector 

class TimingOutQueryFactory(queryFactory: QueryFactory, timeout: Duration) extends QueryFactory {
  def apply(connection: Connection, query: String, params: Any*) = {
    new TimingOutQuery(queryFactory(connection, query, params: _*), timeout)
  }
}

