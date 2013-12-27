package asapstack

import java.sql.{Array => SqlArray, _}
import javax.sql._
import scala.collection.mutable.{ArrayBuffer, HashMap => MutableHashMap}
import scala.collection.immutable.Vector
import java.io.File
import java.security.MessageDigest

object DB {
  private val MigrationPath = "migration"
  val DefaultDatabase = "asap"
  private var connectionString = "jdbc:postgresql:dan"
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

  private lazy val migrationHistoryExists = {
    val migrationHistory = executeQuery(
      "select 1 from information_schema.tables where table_name = 'migration_history' and " +
      s"table_catalog = '$DefaultDatabase'")
    migrationHistory match {
      case None => false
      case Some(Vector()) => false
      case _ => true
    }
  }

  def executeMigration(migration: String) = {
    val migrationFiles = listFiles(s"$MigrationPath/$migration")
    migrationFiles.foreach(file => Resources.load(s"/$MigrationPath/$migration/$file").foreach {
      script_content => {
        val md5 = MessageDigest.getInstance("MD5")
        val bytes = script_content.getBytes("UTF-8")
        val digest = md5.digest(bytes).map(b => "%02X".format(b)).mkString
        if (!migrationHistoryExists ||
            executeQuery(s"select 1 from migration_history where script_hash = '$digest'").get.length == 0
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
              Some(statement.executeUpdate())
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
  
  // returns query result as a Vector of Maps for manageability
  def executeQuery(conn: Connection, sql: String): Option[Vector[Map[String, Object]]] = {
    val statement = conn.createStatement
    val rs = statement.executeQuery(sql)
    val result = resultSetToVector(rs)
    rs.close()
    statement.close()
    Some(result)
  }

  def executeQuery(sql: String): Option[Vector[Map[String, Object]]] = {
    execute(conn => executeQuery(conn, sql))
  }

  def executeUpdate(conn: Connection, sql: String): Option[Int] = {
    val statement = conn.createStatement
    val result = statement.executeUpdate(sql)
    statement.close()
    Some(result)
  }

  def executeUpdate(sql: String): Option[Int] = {
    execute(conn => executeUpdate(conn, sql))
  }

  def executeTransaction[T](f: Connection => Option[T]) = {
    execute {
      conn => {
        try {
          conn.setAutoCommit(false)
          val result = f(conn)
          conn.commit()
          result
        } catch {
          case x: Throwable => {
            conn.rollback()
            x.printStackTrace()
            None
          }
        }
      }
    }
  }

  def execute[T](f: Connection => Option[T]):Option[T] = {
    try {
      val conn = connection
      val result = f(conn)
      conn.close()
      result
    } catch {
      case x: Throwable => x.printStackTrace; None
    }
  }
  init
}
