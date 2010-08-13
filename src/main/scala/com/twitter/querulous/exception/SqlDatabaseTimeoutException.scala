package com.twitter.querulous.exception

import java.sql.SQLException

class SqlDatabaseTimeoutException(msg: String) extends SQLException(msg)
