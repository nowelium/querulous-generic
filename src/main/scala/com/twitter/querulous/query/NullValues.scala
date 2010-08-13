package com.twitter.querulous.query

import java.sql.Types

//
// TODO: deprecated for unduly complicating both usage and implementation
//
// sealed abstract case class NullValue(typeVal: Int)
sealed abstract class NullValue(_typeVal: Int) { def typeVal = _typeVal }

object NullValues {
  case object NullString extends NullValue(Types.VARCHAR)
  case object NullInt extends NullValue(Types.INTEGER)
  case object NullDouble extends NullValue(Types.DOUBLE)
  case object NullBoolean extends NullValue(Types.BOOLEAN)
  case object NullTimestamp extends NullValue(Types.TIMESTAMP)
  case object NullLong extends NullValue(Types.BIGINT)
}
