package asapstack

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import spray.json._
import JsonProtocol._
import reflect.ClassTag
import spray.httpx.SprayJsonSupport._


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
            "test1"
          }
        }
      } ~
      path("test2") {
        get {
          complete {
            "test2"
          }
        }
      }
    }
  }

  val dbRoute = {
    pathPrefix("db") {
      path("tables") {
        get {
          complete {
            val result = DB.executeQuery("select * from information_schema.tables")
            val x:Array[Map[String,Object]] = result.toArray
            x
            //Array[Any](1,2,3,Array(1,2,3))
            //Color("hey", 1, 2, 3)
            //val m = Map.empty[String, Object] + ("hey" -> "man") + ("yoyo" -> "ma")
            //Array[Map[String,Object]](m, m)
          }
        }
      }
    }
  }

  val myRoute = testRoute ~ staticRoute ~ apiRoute ~ dbRoute
}
