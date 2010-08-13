package com.twitter.querulous.database

import java.sql.Connection
import com.twitter.xrayspecs.TimeConversions._

trait Database {
  def open(): Connection

  def close(connection: Connection)

  def withConnection[A](f: Connection => A): A = {
    val connection = open()
    try {
      f(connection)
    } finally {
      close(connection)
    }
  }
}
