package com.twitter.querulous.evaluator

import java.sql.ResultSet
import java.sql.{SQLException, SQLIntegrityConstraintViolationException}
import com.twitter.xrayspecs.{Time, Duration}
import com.twitter.xrayspecs.TimeConversions._

abstract class QueryEvaluatorProxy(queryEvaluator: QueryEvaluator) extends QueryEvaluator {
  def select[A](query: String, params: Any*)(f: ResultSet => A) = {
    delegate(queryEvaluator.select(query, params: _*)(f))
  }

  def selectOne[A](query: String, params: Any*)(f: ResultSet => A) = {
    delegate(queryEvaluator.selectOne(query, params: _*)(f))
  }

  def execute(query: String, params: Any*) = {
    delegate(queryEvaluator.execute(query, params: _*))
  }

  def call[A](query: String, params: Any*)(f: ResultSet => A) = {
    delegate(queryEvaluator.call(query, params: _*)(f))
  }

  def count(query: String, params: Any*) = {
    delegate(queryEvaluator.count(query, params: _*))
  }

  def transaction[T](f: Transaction => T) = {
    delegate(queryEvaluator.transaction(f))
  }

  protected def delegate[A](f: => A) = f
}
