package com.twitter.querulous.query

import java.sql.{PreparedStatement, ResultSet, SQLException, Timestamp, Types, Connection}
import java.util.regex.Pattern
import scala.collection.mutable

class SqlQueryFactory extends QueryFactory {
  def apply(connection: Connection, query: String, params: Any*) = {
    new SqlQuery(connection, query, params: _*)
  }
}
