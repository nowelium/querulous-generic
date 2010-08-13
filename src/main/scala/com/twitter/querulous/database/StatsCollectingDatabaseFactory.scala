package com.twitter.querulous.database

import com.twitter.querulous.StatsCollector 

import java.sql.Connection

class StatsCollectingDatabaseFactory(
  databaseFactory: DatabaseFactory,
  stats: StatsCollector) extends DatabaseFactory {

  def apply(jdbcDriver: String, jdbcUrl: String, username: String, password: String) = {
    new StatsCollectingDatabase(databaseFactory(jdbcDriver, jdbcUrl, username, password), stats)
  }
}
