package com.twitter.querulous.database

import org.apache.commons.dbcp.DriverManagerConnectionFactory
import java.sql.{SQLException, Connection}

class SingleConnectionDatabaseFactory extends DatabaseFactory {
  def apply(jdbcDriver: String, jdbcUrl: String, username: String, password: String) = {
    new SingleConnectionDatabase(jdbcDriver, jdbcUrl, username, password)
  }
}
