package asapstack

import java.sql.{Array => SqlArray, _}
import scala.collection.mutable.{ArrayBuffer, HashMap => MutableHashMap}
import scala.collection.immutable.Vector
import java.io.File
import java.security.MessageDigest

object DB {
  val MigrationPath = "migration"
  val DefaultDatabase = "asap"
  var connectionString = "jdbc:postgresql:dan"
  def connection = DriverManager.getConnection(connectionString, "dan", "password")


  def init = {
    Class.forName("org.postgresql.Driver")
    val conn = connection
    val statement = conn.createStatement
    val rs = statement.executeQuery(s"select 1 from pg_catalog.pg_database where datname = '$DefaultDatabase'")
    if (!rs.next()) {
      val create = conn.createStatement
      statement.executeUpdate(s"create database $DefaultDatabase")
    }
    connectionString = s"jdbc:postgresql:$DefaultDatabase"
    executeAllMigrations()
  }

  private def isAllDigits(str: String) = {
    !str.matches("[^\\d]")
  }

  private def listFiles(path: String) = {
    Resources.loadLines(s"/$path/list").toSeq.flatMap(x => x).map(s => s.trim)
  }

  lazy val migrationHistoryExists = {
    val migrationHistory = executeQuery(
      "select 1 from information_schema.tables where table_name = 'migration_history' and " +
      s"table_catalog = '$DefaultDatabase'")
    migrationHistory.length > 0
  }

  def executeMigration(migration: String) = {
    val migrationFiles = listFiles(s"$MigrationPath/$migration")
    migrationFiles.foreach(file => Resources.load(s"/$MigrationPath/$migration/$file").foreach {
      script_content => {
        val md5 = MessageDigest.getInstance("MD5")
        val bytes = script_content.getBytes("UTF-8")
        val digest = md5.digest(bytes).map(b => "%02X".format(b)).mkString
        if (!migrationHistoryExists ||
            executeQuery(s"select 1 from migration_history where script_hash = '$digest'").length == 0
          ) {
          executeUpdate(script_content)
          execute {
            conn => {
              val statement = conn.prepareStatement(
                "insert into migration_history (migration, script_name, script_hash, script_content, ran_at) " +
                "values (?, ?, ?, ?, ?)")
              statement.setString(1, migration)
              statement.setString(2, file)
              statement.setString(3, digest)
              statement.setString(4, script_content)
              statement.setTimestamp(5, new Timestamp((new java.util.Date).getTime))
              statement.executeUpdate()
            }
          }
        }
      }
    })
  }

  def executeAllMigrations() = {
    val migrations = listFiles(MigrationPath);
    migrations.foreach(migration => executeMigration(migration))
  }

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
    println(sql)
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
