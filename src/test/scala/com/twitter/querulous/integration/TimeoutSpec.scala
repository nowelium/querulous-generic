package com.twitter.querulous.integration

import java.util.concurrent.CountDownLatch
import org.specs.Specification
import net.lag.configgy.Configgy
import com.twitter.xrayspecs.Time
import com.twitter.xrayspecs.TimeConversions._
import com.twitter.querulous.query.{SqlQueryFactory, TimingOutQueryFactory, SqlQueryTimeoutException}
import com.twitter.querulous.evaluator.{StandardQueryEvaluatorFactory, QueryEvaluator}


class TimeoutSpec extends Specification {
  Configgy.configure("config/test.conf")

  import com.twitter.querulous.TestEvaluator._

  val config = Configgy.config.configMap("db")
  val username = config("username")
  val password = config("password")
  val timeout = 1.second
  val timingOutQueryFactory = new TimingOutQueryFactory(testQueryFactory, timeout)
  val timingOutQueryEvaluatorFactory = new StandardQueryEvaluatorFactory(testDatabaseFactory, timingOutQueryFactory)

  doBefore {
    testEvaluatorFactory("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:querulous", username, password).execute("CREATE DATABASE IF NOT EXISTS db_test")
  }
  "Timeouts" should {
//
//    "honor timeouts" in {
//      val queryEvaluator1 = testEvaluatorFactory("", "db_test", username, password)
//      val latch = new CountDownLatch(1)
//      val thread = new Thread() {
//        override def run() {
//          queryEvaluator1.select("SELECT GET_LOCK('padlock', 1) AS rv") { row =>
//            latch.countDown()
//            try {
//              Thread.sleep(60.seconds.inMillis)
//            } catch {
//              case _ =>
//            }
//          }
//        }
//      }
//      thread.start()
//      latch.await()
//
//      val queryEvaluator2 = timingOutQueryEvaluatorFactory(List("localhost"), "db_test", username, password)
//      val start = Time.now
//      queryEvaluator2.select("SELECT GET_LOCK('padlock', 60) AS rv") { row => row.getInt("rv") } must throwA[SqlQueryTimeoutException]
//      val end = Time.now
//      (end - start).inMillis must beCloseTo(timeout.inMillis, 1.second.inMillis)
//      thread.interrupt()
//      thread.join()
//    }
  }
}
