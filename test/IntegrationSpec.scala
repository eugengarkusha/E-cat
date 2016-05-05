
import java.time.LocalDateTime

import org.scalatest.{FlatSpec, Matchers, WordSpecLike}
import play.api.test._
import play.api.test.Helpers._
import ecat.model.Preprocessing._
import shapeless.record.Record

/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */

class IntegrationSpec extends  FlatSpec with Matchers{

//  "Application" should {
//
//    "work from within a browser" in new WithBrowser {
//
//      browser.goTo("http://localhost:" + port)
//
//      browser.pageSource must contain("Your new application is ready.")
//    }
//  }

"sdada"should  "sdasd" in {
  println(tariffs(Nil,LocalDateTime.of(2016,11,6,12,3,3),LocalDateTime.of(2016,11,5,12,3,3),"SOMEID"))
  }


}
