package com.twitter.querulous.query

import java.sql.{SQLException, Connection}
import com.twitter.xrayspecs.Duration

class RetryingQuery(query: Query, retries: Int) extends QueryProxy(query) {
  override def delegate[A](f: => A): A = delegate(f, retries)

  def delegate[A](f: => A, retries: Int): A = {
    try {
      f
    } catch {
      case e: SQLException =>
        if (retries > 1)
          delegate(f, retries - 1)
        else
          throw e
    }
  }
}
