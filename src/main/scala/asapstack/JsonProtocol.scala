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

  implicit object RootLongJsonFormat extends RootJsonFormat[Long] {
    def write(x: Long) = JsNumber(x)
    def read(value: JsValue) = value match {
      case JsNumber(x) => x.longValue
      case x => deserializationError("Expected Long as JsNumber, but got " + x)
    }
  }
}
