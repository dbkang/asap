package asapstack

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._

class MyServiceSpec extends Specification with Specs2RouteTest with MyService {
  def actorRefFactory = system
  
  "MyService" should {
    "redirect get requests to the root path to the app path" in {
      Get("/") ~> myRoute ~> check {
        responseAs[String] must contain("/dynamic")
        status === StatusCodes.MovedPermanently
        val renderer = new StringRendering
        header[HttpHeaders.Location].get.uri.render(renderer)
        renderer.get  === "/dynamic/"
      }
    }
    
    "return GET requests to the app path" in {
      Get("/dynamic") ~> myRoute ~> check {
        responseAs[String] must contain("ASAP")
      }
      Get("/dynamic/blahblah") ~> myRoute ~> check {
        responseAs[String] must contain("ASAP")
      }
      Get("/dynamic/") ~> myRoute ~> check {
        responseAs[String] must contain("ASAP")
      }
    }
    
    "leave GET requests to other paths unhandled" in {
      Get("/kermit") ~> myRoute ~> check {
        handled must beFalse
      }
    }
    
    "return a MethodNotAllowed error for PUT requests to the root path" in {
      Put() ~> sealRoute(myRoute) ~> check {
        status === MethodNotAllowed
        responseAs[String] === "HTTP method not allowed, supported methods: GET"
      }
    }
  }
}
