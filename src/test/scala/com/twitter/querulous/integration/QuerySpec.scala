package com.twitter.querulous.integration

import org.specs.Specification
import net.lag.configgy.Configgy
import com.twitter.xrayspecs.Time
import com.twitter.xrayspecs.TimeConversions._
import com.twitter.querulous.database.ApachePoolingDatabaseFactory
import com.twitter.querulous.query._
import com.twitter.querulous.evaluator.{StandardQueryEvaluatorFactory, QueryEvaluator}
import com.twitter.querulous.exception.TooManyQueryParametersException
import com.twitter.querulous.exception.TooFewQueryParametersException

class QuerySpec extends Specification {
  Configgy.configure("config/" + System.getProperty("stage", "test") + ".conf")

  import com.twitter.querulous.TestEvaluator._
  val queryEvaluator = testEvaluatorFactory("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:querulous", "sa", "")

  "Query" should {
    doBefore {
      queryEvaluator.execute("CREATE TABLE foo ( bar VARCHAR(10) )")
    }
    doAfter {
      queryEvaluator.execute("DROP TABLE foo")
    }

    "with too many arguments" >> {
      queryEvaluator.select("SELECT 1 FROM foo WHERE 1 IN (?)", 1, 2, 3) { r => 1 } must throwA[TooManyQueryParametersException]
    }

    "with too few arguments" >> {
      queryEvaluator.select("SELECT 1 FROM foo WHERE 1 = ? OR 1 = ?", 1) { r => 1 } must throwA[TooFewQueryParametersException]
    }

    "with just the right number of arguments" >> {
      queryEvaluator.select("SELECT count(*) FROM foo WHERE 1 IN (?)", List(1, 2, 3))(_.getInt(1)).toList mustEqual List(0)
    }
  }
}
