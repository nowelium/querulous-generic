package com.twitter.querulous.evaluator

import java.sql.ResultSet
import com.twitter.querulous.database.{Database, DatabaseFactory}
import com.twitter.querulous.query.QueryFactory

class StandardQueryEvaluatorFactory(
  databaseFactory: DatabaseFactory,
  queryFactory: QueryFactory) extends QueryEvaluatorFactory {

  def apply(jdbcDriver: String, jdbcUrl: String, username: String, password: String) = {
    val database = databaseFactory(jdbcDriver, jdbcUrl, username, password)
    new StandardQueryEvaluator(database, queryFactory)
  }
}

class StandardQueryEvaluator(protected val database: Database, queryFactory: QueryFactory)
  extends QueryEvaluator {

  def select[A](query: String, params: Any*)(f: ResultSet => A) = withTransaction(_.select(query, params: _*)(f))
  def selectOne[A](query: String, params: Any*)(f: ResultSet => A) = withTransaction(_.selectOne(query, params: _*)(f))
  def count(query: String, params: Any*) = withTransaction(_.count(query, params: _*))
  def execute(query: String, params: Any*) = withTransaction(_.execute(query, params: _*))

  def transaction[T](f: Transaction => T) = {
    withTransaction { transaction =>
      transaction.begin()
      try {
        val rv = f(transaction)
        transaction.commit()
        rv
      } catch {
        case e: Throwable =>
          transaction.rollback()
          throw e
      }
    }
  }

  private def withTransaction[A](f: Transaction => A) = {
    database.withConnection { connection => f(new Transaction(queryFactory, connection)) }
  }

  override def equals(other: Any) = {
    other match {
      case other: StandardQueryEvaluator =>
        database eq other.database
      case _ =>
        false
    }
  }

  override def hashCode = database.hashCode
}
