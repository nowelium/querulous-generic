package com.twitter.querulous.evaluator

import java.sql.ResultSet
import java.sql.{SQLException, SQLIntegrityConstraintViolationException}
import com.twitter.xrayspecs.{Time, Duration}
import com.twitter.xrayspecs.TimeConversions._

class AutoDisablingQueryEvaluatorFactory(
  queryEvaluatorFactory: QueryEvaluatorFactory,
  disableErrorCount: Int,
  disableDuration: Duration) extends QueryEvaluatorFactory {

  private def chainEvaluator(evaluator: QueryEvaluator) = {
    new AutoDisablingQueryEvaluator(
      evaluator,
      disableErrorCount,
      disableDuration)
  }

  def apply(jdbcDriver: String, jdbcUrl: String, username: String, password: String) = {
    chainEvaluator(queryEvaluatorFactory(jdbcDriver, jdbcUrl, username, password))
  }
}
