package com.twitter.querulous.database

import org.apache.commons.dbcp.DriverManagerConnectionFactory
import java.sql.{SQLException, Connection}

class SingleConnectionDatabase(jdbcDriver: String, jdbcUrl: String, username: String, password: String)
  extends Database {
  Class.forName(jdbcDriver)
  private val connectionFactory = new DriverManagerConnectionFactory(jdbcUrl, username, password)

  def close(connection: Connection) {
    try {
      connection.close()
    } catch {
      case _: SQLException =>
    }
  }

  def open() = connectionFactory.createConnection()
  override def toString = jdbcUrl
}
