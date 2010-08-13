package com.twitter.querulous.database

import com.twitter.querulous.StatsCollector 

import java.sql.Connection

class StatsCollectingDatabase(database: Database, stats: StatsCollector)
  extends Database {

  override def open(): Connection = {
    stats.time("database-open-timing") {
      database.open()
    }
  }

  override def close(connection: Connection) = {
    stats.time("database-close-timing") {
      database.close(connection)
    }
  }
}
