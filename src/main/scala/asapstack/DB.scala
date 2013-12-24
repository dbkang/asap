package asapstack

import java.sql.{Array => SqlArray, _}
import scala.collection.mutable.{ArrayBuffer, HashMap => MutableHashMap}
import scala.collection.immutable.Vector

object DB {
  def init = {
    Class.forName("org.postgresql.Driver")
    val conn = connection
    val statement = conn.createStatement
    val rs = statement.executeQuery(s"select 1 from pg_catalog.pg_database where datname = '$defaultDatabase'")
    if (!rs.next()) {
      val create = conn.createStatement
      statement.executeUpdate(s"create database $defaultDatabase")
    }
    connectionString = s"jdbc:postgresql:$defaultDatabase"
  }

  var connectionString = "jdbc:postgresql:dan"

  def connection = DriverManager.getConnection(connectionString, "dan", "password")

  val defaultDatabase = "asap"

  def resultSetToVector(rs: ResultSet) = {
    val metadata = rs.getMetaData
    val colCount = metadata.getColumnCount
    val colNames = Array.ofDim[String](colCount + 1)
    for (i <- 1 to colCount) {
      colNames(i) = metadata.getColumnName(i)
    }
    val result = ArrayBuffer.empty[Map[String, Object]]
    while (rs.next()) {
      val map = MutableHashMap.empty[String, Object]
      for (i <- 1 to colCount) {
        map += (colNames(i) -> rs.getObject(i))
      }
      result += map.toMap
    }
    result.toVector
  }
  
  // returns query result as an ArrayBuffer of mutable HashMaps for manageability
  def executeQuery(sql: String) = {
    execute {
      conn => {
        val statement = conn.createStatement
        val rs = statement.executeQuery(sql)
        val result = resultSetToVector(rs)
        rs.close()
        statement.close()
        result
      }
    }
  }

  def executeUpdate(sql: String) = {
    execute {
      conn => {
        val statement = conn.createStatement
        val result = statement.executeUpdate(sql)
        statement.close()
        result
      }
    }
  }

  def execute[T](f: Connection => T) = {
    val conn = connection
    val result = f(conn)
    conn.close()
    result
  }
  init
}
