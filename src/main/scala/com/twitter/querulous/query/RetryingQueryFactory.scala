package com.twitter.querulous.query

import java.sql.{SQLException, Connection}
import com.twitter.xrayspecs.Duration

class RetryingQueryFactory(queryFactory: QueryFactory, retries: Int) extends QueryFactory {
  def apply(connection: Connection, query: String, params: Any*) = {
    new RetryingQuery(queryFactory(connection, query, params: _*), retries)
  }
}
