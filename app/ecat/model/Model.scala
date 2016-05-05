package ecat.model

import java.time.temporal.ChronoUnit
import java.time.{LocalDateTime, LocalTime}

import ecat.util.DateTime.{pertrovichDateTimeFormatter => fmt, _}
import play.api.libs.json._
import play.api.libs.functional.syntax._
import ecat.util.JsonFormats._
import ecat.util.RecordInstances._

import scala.xml.Node
import scalaz.{Ordering => _, _}
import Scalaz._
import shapeless._
import record._
import ops.traversable.FromTraversable._
import ops.record.Values
import shapeless.syntax.std.traversable._
import shapeless.contrib.scalaz._
import schema.RecordFilters
import schema.RecordFilters._



/**
 * Created by admin on 10/12/15.
 */

object Schema {
  type Room = Record.`'number->Int, 'guestsCnt->Int, 'additionalGuestsCnt-> Int, 'twin->Boolean, 'bathroom->String, 'level-> Int, 'options-> List[String]`.T
  type Prices = Record.`'room-> Long, 'bkf-> Long, 'eci-> Long, 'lco-> Long`.T
  type Tariff = Record.`'id-> String, 'name->String, 'startDate-> LocalDateTime, 'endDate-> LocalDateTime, 'pricesPerDay -> Prices`.T  //'roomPrice-> Long, 'bkf-> Long, 'eci-> Long, 'lco-> Long`
  type TariffGroup = Record.`'name->String, 'tariffs->List[Tariff], 'overalPrices->Prices`.T
  type Category = Record.`'id-> String, 'name->String, 'rooms-> List[Room], 'tariffGroups-> List[TariffGroup]`.T
  type Hotel =  Record.`'id-> String, 'name-> String, 'checkInTime-> LocalTime, 'checkOutTime-> LocalTime, 'categories-> List[Category]`.T
}

//options - additional details about room setup. They may become the dynamic part of filters .
object Room{

  def fromXml(n: Node):Schema.Room = {

    Record(
      number = n \@ "num" toInt,
      guestsCnt = n \@ "guests" toInt,
      additionalGuestsCnt= n \@ "addguests" toInt,
      twin =  n \@ "twin" toBoolean,
      bathroom = n \@ "bathroom",
      level = (n \@ "level").toInt,
      options = (n \ "option").map(_.text).toList
    )
  }
}


object Tariff{

  def duration(t:Schema.Tariff)=ChronoUnit.DAYS.between(t.get('startDate), t.get('endDate))

  def pricePerDay(t:Schema.Tariff,bkf:Boolean,eci:Boolean,lco:Boolean,roomCnt: Int)={
    val p = t.get('pricesPerDay)
    def addIf(cond: Boolean, l: Long, r:Long) = if (cond) l + r else r

    (roomCnt * addIf(lco, p.get('lco), addIf(eci, p.get('eci), addIf(bkf, p.get('bkf), p.get('room))))).toDouble / 100

  }
  def fromXml(n: Node):Schema.Tariff = Record(
    id = n \@"id" ,
    name = n \@"name" ,
    startDate = LocalDateTime.parse(n \@"dateN", fmt) ,
    endDate = LocalDateTime.parse(n \@"dateK", fmt) ,
    pricesPerDay = Record(
      room = (n \@"roomprice").toDouble * 100 toLong,
      bkf = (n \@"bkfprice").toDouble * 100 toLong,
      eci = (n \@"eciprice").toDouble * 100 toLong,
      lco = (n \@"lcoprice").toDouble * 100 toLong

    )
  )
}



object Prices{
  def fromLong(l: Long) = l.toDouble / 100
//  implicit val pm1 = Monoid[Schema.Prices]
}



object Category {

  def fromXml(n: Node, from: LocalDateTime, to: LocalDateTime): ValidationNel[String, Schema.Category] = {

    val catId =  n \@ "id"
    def name = n \@ "name"
    def rooms: ValidationNel[String, List[Schema.Room]] = Preprocessing.rooms((n \ "room").toList.map(Room.fromXml))
    def tariffGroups: ValidationNel[String, List[Schema.TariffGroup]] = Preprocessing.tariffs((n \ "tarif").toList.map(Tariff.fromXml), from, to, catId)

    (rooms |@| tariffGroups) { (_rooms, _tariffGroups)=> Record(id = catId, name = name, rooms= _rooms , tariffGroups= _tariffGroups )}
    .leftMap(err=>NonEmptyList(s"categoryId=$catId:" + err))

  }

}

  object Hotel {

    def fromXml(n: Node, from: LocalDateTime, to: LocalDateTime): ValidationNel[String, List[Schema.Hotel]] = {

      (n \ "hotel").map { hotelNode =>

        val id = hotelNode \@ "id"
        def name = hotelNode \@ "name"
        def ci = LocalTime.of(hotelNode \@ "ckeckin" toInt, 0)
        def co = LocalTime.of(hotelNode \@ "checkout" toInt, 0)
        def cats:ValidationNel[String, List[Schema.Category]] = {
            (hotelNode \ "category").toList
            .map(c=>Category.fromXml(c, from, to).map(_ :: Nil))
            .reduce(_ +++ _)
            .leftMap(errs => NonEmptyList(s"hotelId=$id:$errs"))
        }

        cats.map(c => Record(id = id, name = name, checkInTime = ci, checkOutTime = co , categories = c) :: Nil)

      }.reduce(_ +++ _)
    }

  }


