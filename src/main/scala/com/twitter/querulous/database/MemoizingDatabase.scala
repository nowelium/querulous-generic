package com.twitter.querulous.database

import scala.collection.mutable

class MemoizingDatabaseFactory(databaseFactory: DatabaseFactory) extends DatabaseFactory {
  private val databases = new mutable.HashMap[String, Database] with mutable.SynchronizedMap[String, Database]

  def apply(jdbcDriver: String, jdbcUrl: String, username: String, password: String) = synchronized {
    databases.getOrElseUpdate(
      jdbcUrl,
      databaseFactory(jdbcDriver, jdbcUrl, username, password)
    )
  }
}

