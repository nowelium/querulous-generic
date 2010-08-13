package com.twitter.querulous.query

import java.sql.{SQLException, Connection}
import com.twitter.xrayspecs.Duration
import com.twitter.querulous.Timeout
import com.twitter.querulous.StatsCollector 
import com.twitter.querulous.exception.TimeoutException
import com.twitter.querulous.exception.SqlQueryTimeoutException

class TimingOutQuery(query: Query, timeout: Duration) extends QueryProxy(query) {
  override def delegate[A](f: => A) = {
    try {
      Timeout(timeout) {
        f
      } {
        cancel()
      }
    } catch {
      case e: TimeoutException =>
        throw new SqlQueryTimeoutException
    }
  }
}
