package asapstack

import spray.json._
import JsonProtocol._
import org.postgresql.util.PGobject

class KeyValueStore(val collection: String) {
  def apply(bucket: String, key: String):JsValue = {
    val query = "select value from key_value_history where collection = ? and bucket = ? and key = ? and " +
    "stamp in (select max(stamp) from key_value_history where collection = ? and bucket = ? and key = ?)"
    val value = DB.execute {
      conn => {
        val statement = conn.prepareStatement(query)
        statement.setString(1, collection)
        statement.setString(2, bucket)
        statement.setString(3, key)
        statement.setString(4, collection)
        statement.setString(5, bucket)
        statement.setString(6, key)
        DB.resultSetToVector(statement.executeQuery()).headOption.map(x => jsonFromPGobject(x("value")))
      }
    }
    value.get
  }

  def jsonFromPGobject(o: Object) = {
    o.asInstanceOf[PGobject].getValue.asJson
  }

  def update(bucket: String, key: String, value: JsValue): JsValue = {
    update(bucket, key, value.compactPrint)
    value
  }

  def update(bucket: String, key: String, value: String): String = {
    val stamp = getStamp
    DB.executeTransaction {
      conn => {
        val sql = "insert into key_value_history (collection, bucket, key, stamp, value) " +
        "values (?, ?, ?, ?, ?)"
        val valueObj = new PGobject
        valueObj.setType("json")
        valueObj.setValue(value)
        val statement = conn.prepareStatement(sql)
        statement.setString(1, collection)
        statement.setString(2, bucket)
        statement.setString(3, key)
        statement.setLong(4, stamp)
        statement.setObject(5, valueObj)
        statement.executeUpdate()
        Some(value)
      }
    }
    value

  }

  def getStamp = {
    val stamp = DB.executeTransaction {
      conn => {
        DB.executeUpdate(conn, "update last_stamp set last_stamp = last_stamp + 1")
        val result = DB.executeQuery(conn, "select last_stamp from last_stamp")
        val stampValue = result.flatMap(_.headOption.map(_("last_stamp").asInstanceOf[java.lang.Long].longValue)).get
        val s = conn.prepareStatement("insert into stamp_time (stamp, universal_time) values (?, ?)")
        s.setLong(1, stampValue)
        s.setTimestamp(2, new java.sql.Timestamp((new java.util.Date).getTime))
        s.executeUpdate()
        Some(stampValue)
      }
    }
    stamp.get
  }
}

object KeyValueStore {
  def apply(collection: String) = new KeyValueStore(collection)
}
