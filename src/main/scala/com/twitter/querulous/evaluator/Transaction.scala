package com.twitter.querulous.evaluator

import java.sql.{ResultSet, SQLException, SQLIntegrityConstraintViolationException, Connection}
import com.twitter.querulous.query.QueryFactory

class Transaction(queryFactory: QueryFactory, connection: Connection) extends QueryEvaluator {
  def select[A](query: String, params: Any*)(f: ResultSet => A) = {
    queryFactory(connection, query, params: _*).select(f)
  }

  def selectOne[A](query: String, params: Any*)(f: ResultSet => A) = {
    val results = select(query, params: _*)(f)
    if (results.isEmpty) None else Some(results.head)
  }

  def count(query: String, params: Any*) = {
    selectOne(query, params: _*)(_.getInt(1)) getOrElse 0
  }

  def execute(query: String, params: Any*) = {
    queryFactory(connection, query, params: _*).execute()
  }

  def begin() = {
    connection.setAutoCommit(false)
  }

  def commit() = {
    connection.commit()
    connection.setAutoCommit(true)
  }

  def rollback() = {
    connection.rollback()
    try {
      connection.setAutoCommit(true)
    } catch {
      case _: SQLException => // it's ok. the connection may be dead.
    }
  }

  def transaction[T](f: Transaction => T) = f(this)
}
