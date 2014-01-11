package asapstack

import spray.json._

// uses KeyValue store to implement a store of values where keys are
// automatically assigned as ids

class AutoStore(val collection: String, val name: String) {
  val kv = KeyValueStore(collection)
  val bucket = s"auto:$name"
  val latestBucket = s"auto:$name:latest"
  val latestKey = "default"

  def insert(value: JsValue) = {
    val stamp = kv.getStamp
    DB.executeTransaction {
      conn => {
        val latest = kv.readOption(conn, latestBucket, latestKey) match {
          case Some(JsNumber(x)) => x.toLongExact + 1
          case _ => 1
        }
        kv.insert(conn, stamp, latestBucket, latestKey, latest)
        kv.insert(conn, stamp, bucket, latest, value)
        latest
      }
    }
  }

  def toSeq = {
    kv(bucket).map(_.toLong)
  }

  def apply(id: Long) = {
    kv(bucket, id)
  }

  def update(id: Long, value: JsValue) = {
    val stamp = kv.getStamp
    DB.executeTransaction {
      conn => {
        kv.insert(conn, stamp, bucket, id, value)
      }
    }
  }

}

object AutoStore {
  def apply(collection: String, name: String) = new AutoStore(collection, name)
}
