package com.twitter.querulous.unit

import java.sql.Connection
import scala.collection.mutable
import net.lag.configgy.{Config, Configgy}
import com.twitter.querulous.database.{ApachePoolingDatabaseFactory, MemoizingDatabaseFactory, Database}
import com.twitter.querulous.evaluator.{StandardQueryEvaluator, StandardQueryEvaluatorFactory, QueryEvaluator}
import com.twitter.querulous.query._
import com.twitter.querulous.StatsCollector
import com.twitter.querulous.test.FakeDatabase
import com.twitter.xrayspecs.Time
import com.twitter.xrayspecs.TimeConversions._
import org.specs.Specification
import org.specs.mock.{ClassMocker, JMocker}


class QueryEvaluatorSpec extends Specification with JMocker with ClassMocker {
  Configgy.configure("config/test.conf")
  import com.twitter.querulous.TestEvaluator._

  "QueryEvaluator" should {
    val queryEvaluator = testEvaluatorFactory("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:querulous", "sa", "")
    val queryFactory = new SqlQueryFactory

    doBefore {
      queryEvaluator.execute("CREATE TABLE foo (bar VARCHAR(50), baz INT)")
    }

    doAfter {
      queryEvaluator.execute("DROP TABLE foo")
    }

    "fromConfig" in {
      val stats = mock[StatsCollector]
      QueryFactory.fromConfig(Config.fromMap(Map.empty), None) must haveClass[SqlQueryFactory]
      QueryFactory.fromConfig(Config.fromMap(Map.empty), Some(stats)) must
        haveClass[StatsCollectingQueryFactory]
      QueryFactory.fromConfig(Config.fromMap(Map("query_timeout_default" -> "10")), None) must
        haveClass[TimingOutQueryFactory]
      QueryFactory.fromConfig(Config.fromMap(Map("retries" -> "10")), None) must
        haveClass[RetryingQueryFactory]
      QueryFactory.fromConfig(Config.fromMap(Map("debug" -> "true")), None) must
        haveClass[DebuggingQueryFactory]

      val config = new Config()
      config.setConfigMap("queries", new Config())
      config("query_timeout_default") = "10"
      QueryFactory.fromConfig(config, Some(stats)) must haveClass[TimingOutStatsCollectingQueryFactory]
    }

    "connection pooling" in {
      val connection = mock[Connection]
      val database = new FakeDatabase(connection, 1.millis)

      "transactionally" >> {
        val queryEvaluator = new StandardQueryEvaluator(database, queryFactory)

        expect {
          one(connection).setAutoCommit(false)
          one(connection).prepareStatement("SELECT 1")
          one(connection).commit()
          one(connection).setAutoCommit(true)
        }

        queryEvaluator.transaction { transaction =>
          transaction.selectOne("SELECT 1") { _.getInt("1") }
        }
      }

      "nontransactionally" >> {
        val queryEvaluator = new StandardQueryEvaluator(database, queryFactory)

        expect {
          one(connection).prepareStatement("SELECT 1")
        }

        var list = new mutable.ListBuffer[Int]
        queryEvaluator.selectOne("SELECT 1") { _.getInt("1") }
      }
    }

    "select rows" in {
      var list = new mutable.ListBuffer[Int]
      queryEvaluator.select("SELECT count(*) as rowcount FROM foo") { resultSet =>
        list += resultSet.getInt("rowcount")
      }
      list.toList mustEqual List(0)
    }

    "transaction" in {
      "when there is an exception" >> {
        try {
          queryEvaluator.transaction { transaction =>
            transaction.execute("INSERT INTO foo VALUES ('1', 1)")
            throw new Exception("oh noes")
          }
        } catch {
          case _ =>
        }

        queryEvaluator.select("SELECT * FROM foo")(_.getInt("bar")).toList mustEqual Nil
      }

      "when there is not an exception" >> {

        queryEvaluator.transaction { transaction =>
          transaction.execute("INSERT INTO foo VALUES (?, ?)", "one", 2)
        }

        queryEvaluator.select("SELECT * FROM foo") { row => (row.getString("bar"), row.getInt("baz")) }.toList mustEqual List(("one", 2))
      }
    }

  }
}
