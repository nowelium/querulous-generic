package com.twitter.querulous.query

import com.twitter.querulous.StatsCollector 

import java.sql.{ResultSet, Connection}

class StatsCollectingQueryFactory(queryFactory: QueryFactory, stats: StatsCollector)
  extends QueryFactory {

  def apply(connection: Connection, query: String, params: Any*) = {
    new StatsCollectingQuery(queryFactory(connection, query, params: _*), stats)
  }
}
