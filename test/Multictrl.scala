

import org.scalatest.FunSuite
import ecat.model.ajax.catctrl.Multictrl


import Multictrl._

class Multictrl extends  FunSuite {


  test("all locked,all covered exclusively"){
    val data = Map(1->List(20,10,1),2->List(10,9,6),3->List(1,1,1) )
    val inp = Map(7->List(10,10,1),12->List(2,5,5))
    assert(limits(data, inp) ==Some(Map(7 -> List(20, 10, 1), 12 -> List(10, 9, 6))))
  }

  test("all locked,not all covered exclusively") {
    val data = Map(1->List(20,10,5),2->List(10,9,6),3->List(1,1,1) )
    val inp = Map(7->List(10,10,1),12->List(2,5,5))
    assert(limits(data, inp) ==Some(Map(7 -> List(20, 10, 5), 12 -> List(10, 9, 6))))
  }

  test("one locked other - not") {
    val data = Map(1->List(20,11,6),2->List(10,9,6),3->List(2,7,12) )
    val inp = Map(7->List(10,10,1),12->List(2,5,5))
    assert(limits(data, inp) ==Some(Map(7 -> List(20, 11, 6), 12 -> List(10, 9, 12))))
  }

  test("all not locked") {
    val data = Map(1->List(20,11,6),2->List(30,13,6),3->List(2,7,12) )
    val inp = Map(7->List(10,10,1),12->List(2,5,5))
    assert(limits(data, inp) ==Some(Map(7 -> List(30, 13, 6), 12 -> List(30, 13, 12))))
  }

  test("data with zeros") {
    val data = Map(1 -> List(20, 10, 5), 2 -> List(10, 9, 6), 3 -> List(30, 0, 2), 4 -> List(27, 1, 1))
    val inp = Map(1 -> List(10, 10, 1), 2 -> List(2, 5, 5), 3 -> List(25, 0, 1))
    assert(limits(data, inp) ==Some(Map(1 -> List(20, 10, 5), 2 -> List(10, 9, 6), 3 -> List(30, 1, 2))))
  }
  test("not covered") {
    val data = Map(1 -> List(20, 10, 5), 2 -> List(10, 9, 6), 3 -> List(10, 9, 6))
    val inp = Map(1 -> List(10, 10, 1), 2 -> List(2, 5, 5), 3 -> List(3, 3, 7))
    assert(limits(data, inp) == None)
  }

  test("not covered (permutations)") {
    val data = Map(1 -> List(20, 25, 40, 1), 2 -> List(11, 10, 10, 2), 3 -> List(12, 30, 10, 2), 4 -> List(10, 30, 40, 1))
    val inp = Map(1 -> List(12, 10, 1, 1), 2 -> List(11, 7, 4, 2), 3 -> List(1, 25, 1, 1), 4 -> List(1, 30, 1, 1), 5 -> List(1, 1, 40, 1))
    assert(limits(data, inp) == None)
  }


}
//  "Application" should {
//
//    "work from within a browser" in new WithBrowser {
//
//      browser.goTo("http://localhost:" + port)
//
//      browser.pageSource must contain("Your new application is ready.")
//    }
//  }