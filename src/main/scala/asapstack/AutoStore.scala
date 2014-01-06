package asapstack


// uses KeyValue store to implement a store of values where keys are
// automatically assigned as ids

class AutoStore(val collection: String, val bucket: String) {

}

object AutoStore {
  def apply(collection: String, bucket: String) = new AutoStore(collection, bucket)
}
