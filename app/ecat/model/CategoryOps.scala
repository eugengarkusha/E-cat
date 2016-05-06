package ecat.model

import ecat.model.Schema.Category
import shapeless._, record._

object CategoryOps {

  def availableRoomCnt(cat:Category, guestCnt: Int): Int = {
    val res = {
      if(guestCnt == 1) cat.get('rooms).size
      else cat.get('rooms).count(_.get('guestsCnt) >= guestCnt)
    }
    assert(res > 0)
    res
  }

  def maxGuestCnt(cat:Category, roomCnt: Int)={
    val res = {
      if(roomCnt == 1)cat.get('rooms).iterator.map(_.get('guestsCnt)).max
      else cat.get('rooms).map(_.get('guestsCnt)).sorted(implicitly[Ordering[Int]].reverse)(roomCnt - 1)
    }
    assert(res > 0)
    res
  }
}
