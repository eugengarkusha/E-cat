package ecat.model.ops

import java.time.LocalDateTime

import ecat.model.Preprocessing
import ecat.model.Schema._
import shapeless._
import record._
import ecat.model.ops._,RoomOps._,TariffOps._
import scala.xml.Node
import scalaz.{NonEmptyList, Category=>_,Ordering=>_,_}, Scalaz._

object CategoryOps {


  def maxGuestCnt(cat:Category, roomCnt: Int)={
    val res = {
      if(roomCnt == 1)cat.get('rooms).iterator.map(_.get('guestsCnt)).max
      else cat.get('rooms).map(_.get('guestsCnt)).sorted(implicitly[Ordering[Int]].reverse)(roomCnt - 1)
    }
    assert(res > 0)
    res
  }

  def maxAddGuestCnt(cat:Category, roomCnt: Int)={
    val res = {
      if(roomCnt == 1)cat.get('rooms).iterator.map(_.get('additionalGuestsCnt)).max
      else cat.get('rooms).map(_.get('additionalGuestsCnt)).sorted(implicitly[Ordering[Int]].reverse)(roomCnt - 1)
    }
    res
  }

  def isBkfAvailable(c:Category) = true

  def fromXml(n: Node, from: LocalDateTime, to: LocalDateTime): ValidationNel[String, Category] = {

    val catId =  n \@ "id"
    def name = n \@ "name"
    def rooms: ValidationNel[String, List[Room]] = Preprocessing.rooms((n \ "room").toList.map(RoomOps.fromXml))
    def tariffGroups: ValidationNel[String, List[TariffGroup]] = Preprocessing.tariffs((n \ "tarif").toList.map(TariffOps.fromXml), from, to, catId)

    (rooms |@| tariffGroups) { (_rooms, _tariffGroups)=> Record(id = catId, name = name, rooms= _rooms , tariffGroups= _tariffGroups )}
      .leftMap(err=>NonEmptyList(s"categoryId=$catId:" + err))

  }
}
