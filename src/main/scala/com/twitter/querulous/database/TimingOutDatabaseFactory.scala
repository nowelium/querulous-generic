package com.twitter.querulous.database

import com.twitter.xrayspecs.Duration

class TimingOutDatabaseFactory(
  databaseFactory: DatabaseFactory,
  poolSize: Int,
  queueSize: Int,
  openTimeout: Duration,
  initialTimeout: Duration,
  maxConnections: Int
) extends DatabaseFactory {
  def apply(jdbcDriver: String, jdbcUrl: String, username: String, password: String) = {
    new TimingOutDatabase(
      databaseFactory(jdbcDriver, jdbcUrl, username, password),
      jdbcDriver,
      jdbcUrl,
      poolSize,
      queueSize,
      openTimeout,
      initialTimeout,
      maxConnections
    )
  }
}
