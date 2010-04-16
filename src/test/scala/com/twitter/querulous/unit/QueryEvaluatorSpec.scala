package com.twitter.querulous.unit

import java.sql.{SQLException, DriverManager, Connection}
import scala.collection.mutable
import com.twitter.querulous.evaluator.{StandardQueryEvaluator, QueryEvaluator}
import com.twitter.querulous.query.{QueryFactory, SqlQueryFactory}
import com.twitter.querulous.test.FakeDatabase
import com.twitter.xrayspecs.Time
import com.twitter.xrayspecs.TimeConversions._
import org.specs.Specification
import org.specs.mock.{ClassMocker, JMocker}

object QueryEvaluatorSpec extends Specification with JMocker with ClassMocker {
  import TestEvaluator._

  "QueryEvaluator" should {
    val queryEvaluator = testEvaluatorFactory("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:querulous", "sa", "")
    val queryFactory = new SqlQueryFactory

    doBefore {
      queryEvaluator.execute("CREATE TABLE foo (bar VARCHAR(50), baz INT)")
    }

    doAfter {
      queryEvaluator.execute("DROP TABLE foo")
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
