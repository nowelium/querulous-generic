package com.twitter.querulous.query

import java.sql.{Statement, PreparedStatement, CallableStatement, ResultSet, SQLException, Timestamp, Types, Connection}
import java.util.regex.Pattern
import scala.collection.mutable
import com.twitter.querulous.query.NullValues._
import com.twitter.querulous.exception.TooFewQueryParametersException
import com.twitter.querulous.exception.TooManyQueryParametersException

class SqlQuery(connection: Connection, query: String, params: Any*) extends Query {

  def select[A](f: ResultSet => A): Seq[A] = {
    val statement = buildStatement(connection, query, params: _*)
    withStatement(statement, {
      statement.executeQuery()
      val rs = statement.getResultSet
      try {
        val finalResult = new mutable.ArrayBuffer[A]
        while (rs.next()) {
          finalResult += f(rs)
        }
        finalResult
      } finally {
        rs.close()
      }
    })
  }

  def execute() = {
    val statement = buildStatement(connection, query, params: _*)
    withStatement(statement, {
      statement.executeUpdate()
    })
  }

  def call[A](f: ResultSet => A): Seq[A] = {
    val statement = buildCallableStatement(connection, query, params: _*)
    withStatement(statement, { 
      statement.executeUpdate()
      val rs = statement.getResultSet
      if(null == rs){
        return Nil
      }
      try {
        val finalResult = new mutable.ArrayBuffer[A]
        while (rs.next()) {
          finalResult += f(rs)
        }
        finalResult
      } finally {
        rs.close()
      }
    })
  }

  def cancel() {
    val statement = buildStatement(connection, query, params: _*)
    try {
      statement.cancel()
      statement.close()
    } catch {
      case _ =>
    }
  }

  private def withStatement[A](statement: Statement, f: => A) = {
    try {
      f
    } finally {
      try {
        statement.close()
      } catch {
        case _ =>
      }
    }
  }

  private def buildStatement(connection: Connection, query: String, params: Any*) = {
    val statement = connection.prepareStatement(expandArrayParams(query, params: _*))
    setBindVariable(statement, 1, params)
    statement
  }

  private def buildCallableStatement(connection: Connection, query: String, params: Any*) = {
    val statement = connection.prepareCall(expandArrayParams(query, params: _*))
    setBindVariable(statement, 1, params)
    statement
  }

  private def expandArrayParams(query: String, params: Any*) = {
    val p = Pattern.compile("\\?")
    val m = p.matcher(query)
    val result = new StringBuffer
    var i = 0
    while (m.find) {
      try {
        val questionMarks = params(i) match {
          case a: Array[Byte] => "?"
          case s: Seq[_] => s.map { _ => "?" }.mkString(",")
          case _ => "?"
        }
        m.appendReplacement(result, questionMarks)
      } catch {
        case e: ArrayIndexOutOfBoundsException => throw new TooFewQueryParametersException
      }
      i += 1
    }
    m.appendTail(result)
    result.toString
  }

  private def setBindVariable[A](statement: PreparedStatement, startIndex: Int, param: A): Int = {
    var index = startIndex

    try {
      param match {
        case s: String =>
          statement.setString(index, s)
        case l: Long =>
          statement.setLong(index, l)
        case i: Int =>
          statement.setInt(index, i)
        case b: Array[Byte] =>
          statement.setBytes(index, b)
        case b: Boolean =>
          statement.setBoolean(index, b)
        case d: Double =>
          statement.setDouble(index, d)
        case t: Timestamp =>
          statement.setTimestamp(index, t)
        case is: Seq[_] =>
          for (i <- is) index = setBindVariable(statement, index, i)
          index -= 1
        case n: NullValue =>
          statement.setNull(index, n.typeVal)
        case _ => throw new IllegalArgumentException("Unhandled query parameter type for " +
          param + " type " + param.asInstanceOf[Object].getClass.getName)
      }
      index + 1
    } catch {
      case e: SQLException => throw new TooManyQueryParametersException
    }
  }

  private def setBindVariable[A](statement: CallableStatement, startIndex: Int, param: A): Int = {
    var index = startIndex

    try {
      param match {
        case s: String =>
          statement.setString(index, s)
        case l: Long =>
          statement.setLong(index, l)
        case i: Int =>
          statement.setInt(index, i)
        case b: Array[Byte] =>
          statement.setBytes(index, b)
        case b: Boolean =>
          statement.setBoolean(index, b)
        case d: Double =>
          statement.setDouble(index, d)
        case t: Timestamp =>
          statement.setTimestamp(index, t)
        case is: Seq[_] =>
          for (i <- is) index = setBindVariable(statement, index, i)
          index -= 1
        case n: NullValue =>
          statement.setNull(index, n.typeVal)
        case _ => throw new IllegalArgumentException("Unhandled query parameter type for " +
          param + " type " + param.asInstanceOf[Object].getClass.getName)
      }
      index + 1
    } catch {
      case e: SQLException => throw new TooManyQueryParametersException
    }
  }
}
