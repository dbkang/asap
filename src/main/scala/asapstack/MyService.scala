package asapstack

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import spray.json._
import JsonProtocol._
import reflect.ClassTag
import spray.httpx.SprayJsonSupport._
import scala.collection.mutable.ArrayBuffer

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService {

  val testRoute = {
    path("test") {
      get {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <h1>Say hello to Dan!</h1>
              </body>
            </html>
          }
        }
      }
    }
  }

  val staticRoute = {
    pathPrefix("static") {
      getFromResourceDirectory("static/")
    } ~
    path("") {
      get {
        redirect("/dynamic/", StatusCodes.MovedPermanently)
      }
    } ~
    pathPrefix("dynamic") {
      getFromResource("static/index.html")
    }
  }

  val apiRoute = {
    pathPrefix("api") {
      path("test1") {
        get {
          complete {
            Resources.load("/static/index.html")
          }
        }
      } ~
      path("test2") {
        get {
          complete {
            "test2"
          }
        }
      } ~
      pathPrefix("db") {
        path("tables") {
          get {
            complete {
              DB.executeQuery("select * from information_schema.tables")
            }
          }
        } ~
        path("keyvalue") {
          get {
            complete {
              KeyValueStore("mycollection")("mybucket", "mykey")
            }
          } ~
          put {
            decompressRequest() {
              entity(as[JsValue]) {
                jsv =>
                  detach() {
                    complete {
                      KeyValueStore("mycollection")("mybucket", "mykey") = jsv
                    }
                  }
              }
            }
          }
        }
      }
    }
  }

  val myRoute = testRoute ~ staticRoute ~ apiRoute
}
