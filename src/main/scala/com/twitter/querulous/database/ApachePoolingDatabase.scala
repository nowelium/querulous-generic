package com.twitter.querulous.database

import java.sql.{SQLException, Connection}
import org.apache.commons.dbcp.{PoolableConnectionFactory, DriverManagerConnectionFactory, PoolingDataSource}
import org.apache.commons.pool.impl.{GenericObjectPool}
import com.twitter.xrayspecs.Duration

class ApachePoolingDatabase(
  jdbcDriver: String,
  jdbcUrl: String,
  username: String,
  password: String,
  minOpenConnections: Int,
  maxOpenConnections: Int,
  validationQuery: String,
  checkConnectionHealthWhenIdleFor: Duration,
  maxWaitForConnectionReservation: Duration,
  checkConnectionHealthOnReservation: Boolean,
  evictConnectionIfIdleFor: Duration) extends Database {

  Class.forName(jdbcDriver)

  private val config = new GenericObjectPool.Config
  config.maxActive = maxOpenConnections
  config.maxIdle = maxOpenConnections
  config.minIdle = minOpenConnections
  config.maxWait = maxWaitForConnectionReservation.inMillis

  config.timeBetweenEvictionRunsMillis = checkConnectionHealthWhenIdleFor.inMillis
  config.testWhileIdle = true
  config.testOnBorrow = checkConnectionHealthOnReservation
  config.minEvictableIdleTimeMillis = evictConnectionIfIdleFor.inMillis

  private val connectionPool = new GenericObjectPool(null, config)
  private val connectionFactory = new DriverManagerConnectionFactory(jdbcUrl, username, password)
  private val poolableConnectionFactory = new PoolableConnectionFactory(
    connectionFactory,
    connectionPool,
    null,
    validationQuery,
    false,
    true
  )
  private val poolingDataSource = new PoolingDataSource(connectionPool)

  def close(connection: Connection) {
    try {
      connection.close()
    } catch {
      case _: SQLException =>
    }
  }

  def open() = poolingDataSource.getConnection()

  override def toString = jdbcUrl
}
