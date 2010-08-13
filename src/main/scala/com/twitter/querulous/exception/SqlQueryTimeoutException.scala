package com.twitter.querulous.exception

import java.sql.SQLException

class SqlQueryTimeoutException extends SQLException("Query timeout")
