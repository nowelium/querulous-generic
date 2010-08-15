package com.twitter.querulous.query

import com.twitter.querulous.StatsCollector
import java.sql.{ResultSet, Connection}
import scala.collection.mutable
import com.twitter.xrayspecs.Duration
import com.twitter.xrayspecs.TimeConversions._
import net.lag.configgy.ConfigMap
import net.lag.logging.Logger

trait Query {
  def select[A](f: ResultSet => A): Seq[A]
  def execute(): Int
  def call[A](f: ResultSet => A): Seq[A]
  def cancel()
}
