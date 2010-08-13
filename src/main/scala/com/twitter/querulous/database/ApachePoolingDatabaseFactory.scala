package com.twitter.querulous.database

import java.sql.{SQLException, Connection}
import org.apache.commons.dbcp.{PoolableConnectionFactory, DriverManagerConnectionFactory, PoolingDataSource}
import org.apache.commons.pool.impl.{GenericObjectPool}
import com.twitter.xrayspecs.Duration

class ApachePoolingDatabaseFactory(
  validationQuery: String,
  minOpenConnections: Int,
  maxOpenConnections: Int,
  checkConnectionHealthWhenIdleFor: Duration,
  maxWaitForConnectionReservation: Duration,
  checkConnectionHealthOnReservation: Boolean,
  evictConnectionIfIdleFor: Duration) extends DatabaseFactory {

  def apply(jdbcDriver: String, jdbcUrl: String, username: String, password: String) = {
    val pool = new ApachePoolingDatabase(
      jdbcDriver,
      jdbcUrl,
      username,
      password,
      minOpenConnections,
      maxOpenConnections,
      validationQuery,
      checkConnectionHealthWhenIdleFor,
      maxWaitForConnectionReservation,
      checkConnectionHealthOnReservation,
      evictConnectionIfIdleFor
    )
    pool
  }
}
