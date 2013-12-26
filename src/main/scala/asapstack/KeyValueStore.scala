package asapstack

class KeyValueStore(val collection: String) {
  

}

object KeyValueStore {
  def apply(collection: String) = new KeyValueStore(collection)
}
