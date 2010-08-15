package com.twitter.querulous.evaluator

import com.twitter.querulous.database.DatabaseFactory
import com.twitter.querulous.query.QueryFactory

class StandardQueryEvaluatorFactory(
  databaseFactory: DatabaseFactory,
  queryFactory: QueryFactory
) extends QueryEvaluatorFactory {

  def apply(jdbcDriver: String, jdbcUrl: String, username: String, password: String) = {
    val database = databaseFactory(jdbcDriver, jdbcUrl, username, password)
    new StandardQueryEvaluator(database, queryFactory)
  }
}

