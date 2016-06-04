

import org.scalatest.FunSuite
import ecat.model.ajax.catctrl.MiscFunctions


import MiscFunctions._

class MiscFunctions extends  FunSuite {


  test("all locked,all covered exclusively"){
    val data = List(List(20,10,1),List(10,9,6),List(1,1,1))
    val inp = Map(7->List(10,10,1),12->List(2,5,5))
    assert(limits(data, inp) ==Some(Map(7 -> List(20, 10, 1), 12 -> List(10, 9, 6))))
  }

  test("all locked,not all covered exclusively") {
    val data = List(List(20,10,5),List(10,9,6),List(1,1,1) )
    val inp = Map(7->List(10,10,1),12->List(2,5,5))
    assert(limits(data, inp) ==Some(Map(7 -> List(20, 10, 5), 12 -> List(10, 9, 6))))
  }

  test("one locked other - not") {
    val data = List(List(20,11,6),List(10,9,6),List(2,7,12) )
    val inp = Map(7->List(10,10,1),12->List(2,5,5))
    assert(limits(data, inp) ==Some(Map(7 -> List(20, 11, 6), 12 -> List(10, 9, 12))))
  }

  test("all not locked") {
    val data = List(List(20,11,6),List(30,13,6),List(2,7,12) )
    val inp = Map(7->List(10,10,1),12->List(2,5,5))
    assert(limits(data, inp) ==Some(Map(7 -> List(30, 13, 6), 12 -> List(30, 13, 12))))
  }

  test("data with zeros") {
    val data = List(List(20, 10, 5), List(10, 9, 6), List(30, 0, 2),  List(27, 1, 1))
    val inp = Map(1 -> List(10, 10, 1), 2 -> List(2, 5, 5), 3 -> List(25, 0, 1))
    assert(limits(data, inp) ==Some(Map(1 -> List(20, 10, 5), 2 -> List(10, 9, 6), 3 -> List(30, 1, 2))))
  }
  test("not covered") {
    val data = List(List(20, 10, 5), List(10, 9, 6), List(10, 9, 6))
    val inp = Map(1 -> List(10, 10, 1), 2 -> List(2, 5, 5), 3 -> List(3, 3, 7))
    assert(limits(data, inp) == None)
  }

  test("not covered (permutations)") {
    val data = List(List(20, 25, 40, 1),List(11, 10, 10, 2), List(12, 30, 10, 2), List(10, 30, 40, 1))
    val inp = Map(1 -> List(12, 10, 1, 1), 2 -> List(11, 7, 4, 2), 3 -> List(1, 25, 1, 1), 4 -> List(1, 30, 1, 1), 5 -> List(1, 1, 40, 1))
    assert(limits(data, inp) == None)
  }

  test ("small groups are covered first"){
  val inp  = Map((5,List(1, 0, 0)),
    (10,List(1, 0, 0)),
    (1,List(1, 0, 1)),
    (6,List(1, 0, 0)),
    (9,List(1, 0, 0)),
    (2,List(1, 0, 0)),
    (7,List(1, 0, 0)),
    (3,List(1, 0, 0)),
    (11,List(1, 0, 0)),
    (8,List(1, 0, 0)),
    (4,List(1, 0, 0)))

    val data = List(
      List(2, 0, 0),
        List(2, 0, 0),
        List(2, 0, 0),
        List(2, 0, 0),
        List(2, 0, 0),
        List(2, 0, 0),
        List(2, 0, 0),
        List(2, 0, 1),
        List(2, 0, 0),
        List(2, 0, 0),
        List(2, 0, 0),
        List(2, 0, 1)
    )
    val lim = List(2, 0, 1)
    assert(limits(data, inp) == Some(Map(5 -> lim, 10 -> lim, 1 -> lim, 6 -> lim, 9 -> lim, 2 -> lim, 7 -> lim, 3 -> lim, 11 -> lim, 8 -> lim, 4 -> lim)))
  }

  test("intersecting coverage (123, 234)"){
    val dt = List(List(3,3,3,5,0), List(3,3,3,5,0),List(2,10,10,50,0),List(3,3,3,0,0))
    val inp  = List(List(2,2,2,5,0),List(2,2,2,5,0),List(3,3,3,0,0),List(3,3,3,0,0)).zipWithIndex.map(_.swap).toMap
    assert(limits(dt, inp) ==Some(Map(0 -> List(3, 10, 10, 50, 0), 1 -> List(3, 10, 10, 50, 0), 2 -> List(3, 3, 3, 5, 0), 3 -> List(3, 3, 3, 5, 0))))
  }

}
