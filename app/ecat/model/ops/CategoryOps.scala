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


  def maxGuestCnt(c:Category):Int =c.get('rooms).iterator.map(_.get('guestsCnt)).max

  def maxAddGuestCnt(c:Category):Int = c.get('rooms).iterator.map(_.get('additionalGuestsCnt)).max

  //TODO: remove this method, bkf availability is controlled by tariffs only
  def isBkfAvailable(c: Category): Boolean = {

    c.get('tariffGroups).exists{ tg =>
      val tariffs = tg.get('tariffs)
      val bkfNaCnt = tariffs.count(_.get('pricesPerDay).get('bkf).isEmpty)
      bkfNaCnt == 0
      //covered by validation
      //if (!bkfAvlaiable && bkfNaCnt < tariffs.size)println("tariff group has inconsistent breakfast availability\n"+tg)
    }
  }

  //twin may be available in room but blocked by tariff(see TariffOps.isTwinAvailable)!!
  def isTwinAvailable(c:Category): Boolean = c.get('rooms).find(_.get('twin)).isDefined


  def fromXml(n: Node, from: LocalDateTime, to: LocalDateTime): ValidationNel[String, Category] = {

    val catId =  n \@ "id"
    def name = n \@ "name"
    def rooms: ValidationNel[String, List[Room]] = Preprocessing.rooms((n \ "room").toList.map(RoomOps.fromXml))
    def tariffGroups: ValidationNel[String, List[TariffGroup]] = Preprocessing.tariffs((n \ "tarif").toList.map(TariffOps.fromXml), from, to, catId)

    (rooms |@| tariffGroups) { (_rooms, _tariffGroups) => Record(id = catId, name = name, rooms = _rooms , tariffGroups = _tariffGroups )}
    .leftMap(err => NonEmptyList(s"categoryId=$catId:" + err))

  }
}
