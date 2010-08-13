package com.twitter.querulous.query

import java.sql.{Timestamp, Connection}

class DebuggingQueryFactory(queryFactory: QueryFactory, log: String => Unit) extends QueryFactory {
  def apply(connection: Connection, query: String, params: Any*) = {
    new DebuggingQuery(queryFactory(connection, query, params: _*), log, query, params)
  }
}
