package ecat.model.ops

import java.time.LocalDate

import ecat.model.Schema._
import shapeless._
import record._
import ecat.model.ops._
import scala.xml.Node
import scalaz.{Category => _, Ordering => _, _}
import Scalaz._
import ecat.model.Validate
import ValidationOps._catch

object CategoryOps {


  def maxGuestCnt(c:Category):Int =c.get('rooms).iterator.map(_.get('guestsCnt)).max

  def maxAddGuestCnt(c:Category):Int = c.get('rooms).iterator.map(_.get('additionalGuestsCnt)).max

  @deprecated("remove this method, bkf availability is controlled by tariffs only", "x")
  def isBkfAvailable(c: Category): Boolean = {
    c.get('tariffs).exists(_.get('overallPrices).get('bkf).isDefined)
  }

  //twin may be available in room but disabled for particular tariff(see TariffOps.isTwinAvailable)!!
  def isTwinAvailable(c: Category): Boolean = c.get('rooms).find(_.get('twin)).isDefined


  def fromXml(n: Node, from: LocalDate, to: LocalDate): \/[String, Category] = {
    _catch("exception while parsing Category payload"){
      val id =  n \@ "id"
      val name = n \@ "name"
      val roomNodes = (n \ "room").toList
      val tariffNodes = (n \ "tarif").toList
      (for{
        rooms <- roomNodes.traverseU(RoomOps.fromXml)
        _rawTariffs <- tariffNodes.traverseU(tn => TariffOps.rawFromXml(tn).flatMap(Validate.tariff(_, from, to))).flatMap(Validate.tariff.group)
        tariffs = TariffOps.addOverallPrices(_rawTariffs,from, to)
      } yield Record(id = id, name = name, rooms = rooms , tariffs = tariffs))
      .leftMap(err => s"categoryId=$id: $err")
    }.join
  }
}
