package asapstack

import spray.json._

object JsonProtocol extends DefaultJsonProtocol {
  implicit object ObjectJsonFormat extends RootJsonFormat[Object] {
    def write(o: Object) = {
      if (o == null)
        JsNull
      else
        JsString(o.toString)
    }
    def read(value: JsValue) = value
  }
  implicit object JsValueJsonFormat extends RootJsonFormat[JsValue] {
    def write(js: JsValue) = js
    def read(js: JsValue) = js
  }
}
