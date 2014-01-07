package asapstack

import spray.json._

// uses KeyValue store to implement a store of values where keys are
// automatically assigned as ids

class AutoStore(val collection: String, val name: String) {
  val kv = KeyValueStore(collection)
  val bucket = s"auto:$name"
  val latestKey = s"auto:$name:latest"
  
  def insert(value: JsValue) = {
    
  }

}

object AutoStore {
  def apply(collection: String, name: String) = new AutoStore(collection, name)
}
