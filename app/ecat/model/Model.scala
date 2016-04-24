package ecat.model

import java.time.temporal.ChronoUnit
import java.time.{LocalDateTime, LocalTime}

import ecat.util.DateTime.{pertrovichDateTimeFormatter => fmt, _}
import play.api.libs.json._
import play.api.libs.functional.syntax._
import ecat.util.JsonFormats._
import scala.xml.Node
import scalaz.{Ordering => _, _}
import Scalaz._
import shapeless.contrib.scalaz.instances._


/**
 * Created by admin on 10/12/15.
 */

//options - additional details about room setup. They may become the dynamic part of filters .
case class Room(number:Int, guestsCnt: Int, additionalGuestsCnt: Int, twin:Boolean, bathroom:String, level: Int, options: Seq[String])
object Room{

  def fromXml(n: Node): Validation[String, Room] = {
    val options = (n \ "option")

    Success(options)//.ensure("room options may not be empty")(_.isEmpty)
    .map(opts =>
      Room(n \@ "num" toInt,
        n \@ "guests" toInt,
        n \@ "addguests" toInt,
        n \@ "twin" toBoolean,
        n \@ "bathroom",
        (n \@ "level").toInt,
        opts.map(_.text))
    )
  }

  implicit val rw = Json.format[Room]

}


case class Tariff(id: String, name:String, startDate: LocalDateTime, endDate: LocalDateTime, roomPrice: Long, bkf: Long, eci: Long, lco: Long)

object Tariff{
  def fromXml(n: Node):Tariff = Tariff(
    n \@"id" ,
    n \@"name" ,
    LocalDateTime.parse(n \@"dateN", fmt) ,
    LocalDateTime.parse(n \@"dateK", fmt) ,
    (n \@"roomprice").toDouble * 100 toLong,
    (n \@"bkfprice").toDouble * 100 toLong,
    (n \@"eciprice").toDouble * 100 toLong,
    (n \@"lcoprice").toDouble * 100 toLong
  )

  implicit val tw = Json.format[Tariff]
}


case class Prices(room: Long, bkf: Long, eci: Long, lco: Long)

object Prices{
  def fromLong(l: Long) = l.toDouble / 100
  implicit val pm = Monoid[Prices]
  implicit val pf = Json.format[Prices]
}

case class Category (id: String, name:String, rooms: Seq[Room], tariffs: Seq[Tariff], prices: Prices){
  def maxGuestCnt(roomCnt: Int) = rooms.sortBy(_.guestsCnt)(Ordering[Int].reverse)(roomCnt - 1).guestsCnt
  def maxRoomCnt (guestCnt: Int) = rooms.filter(_.guestsCnt >= guestCnt).size
}


object Category {

  def fromXml(n: Node, from: LocalDateTime, to: LocalDateTime): ValidationNel[String, Category] = {

    val catId = n \@ "id"

    val catName = n \@ "name"

    val errLbl =s"categoryId=$catId:"

  //todo: move validation code to separate file!!!!
    val tariffs: ValidationNel[String, Seq[Tariff]] = {

      val _tariffs = (n \ "tarif" map (Tariff.fromXml))

      val filtered = _tariffs.sortBy(_.startDate)
        .dropWhile(_.endDate.compareTo(from) < 0)
        .takeWhile(_.startDate.compareTo(to) <= 0)

      if (filtered.isEmpty) s"no active tariffs in category $this".failure
      else if (filtered.head.startDate.compareTo(from) > 0 || filtered.last.endDate.compareTo(to) < 0){
        s"""tariffs are not covering date interval: first tariff start date :${_tariffs.head.startDate},
            |last tariff end date:${_tariffs.last.endDate}.Provided dates: $from:, $to.
        |""".stripMargin.failure
      }
      else {
        filtered.sliding(2, 1).find {
          case Seq(t) => false
          case Seq(p, n) => p.endDate != n.startDate
          case Seq() => false
        }.toFailure{
          val t = (filtered.head.copy(startDate = from) +: filtered.tail)
          t.init :+ t.last.copy(endDate = to)
        }.leftMap(s => s"gap or overlap between tariffs: 1)${s(0)}, 2)${s(1)}")
      }
    }.leftMap(err=>NonEmptyList(errLbl + err))



    val rooms: ValidationNel[String, List[Room]] ={
      n \ "room" map (Room.fromXml(_).bimap(er=>NonEmptyList(er), _ :: Nil)) reduce(_ +++ _)
    }


    (tariffs |@| rooms).apply{ (_tariffs, _rooms)=>

      def prices = _tariffs.map { tariff =>
        val dif = ChronoUnit.DAYS.between(tariff.startDate, tariff.endDate) + 1
        Prices(tariff.roomPrice * dif, tariff.bkf * dif, tariff.eci * dif, tariff.lco * dif)
      }.reduce(_ |+| _)

      Category(catId, catName, _rooms, _tariffs, prices)
    }

  }
  implicit val cw = Json.writes[Category]
  implicit val cr:Reads[Category] ={
    (__ \ "id").read[String] and
    (__ \ "name").read[String] and
    (__ \ "rooms").read[Seq[Room]] and
    (__ \ "tariffs").read[Seq[Tariff]]
  }.apply((i,n,r,t) => Category(i,n,r,t,Prices(1000,100,150,250)))
}


  case class Hotel(id: String, name: String, checkInTime: LocalTime, checkOutTime: LocalTime, categories: Seq[Category])

  object Hotel {

    def fromXml(n: Node, from: LocalDateTime, to: LocalDateTime): ValidationNel[String, Seq[Hotel]] = {

      (n \ "hotel").map { hotelNode =>

        val hotelId = hotelNode \@ "id"

        (hotelNode \ "category").map(catNode=>
          Category.fromXml(catNode, from, to)
          .map(_ :: Nil)
        ).reduce(_ +++ _).map(cats =>
          Hotel(
            hotelId,
            hotelNode \@ "name",
            LocalTime.of(hotelNode \@ "ckeckin" toInt, 0),
            LocalTime.of(hotelNode \@ "checkout" toInt, 0),
            cats
          ) :: Nil
        ).leftMap(err=> NonEmptyList(s"hotelId=$hotelId:$err"))
      }.reduce(_ +++ _)
    }


    implicit val hw = Json.format[Hotel]

  }


