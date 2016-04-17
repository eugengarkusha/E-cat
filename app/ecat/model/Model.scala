package ecat.model

import java.time.temporal.ChronoUnit
import java.time.{LocalDateTime, LocalTime}

import ecat.util.DateTime.{pertrovichDateTimeFormatter => fmt, _}
import play.api.libs.json.Json
import ecat.util.JsonFormats._
import scala.xml.Node
import scalaz.{Ordering=>_, _}
import Scalaz._
import shapeless.contrib.scalaz.instances._


/**
 * Created by admin on 10/12/15.
 */

//options - additional details about room setup. They may become the dynamic part of filters .
case class Room(number:Int, guestsCnt: Int, additionalGuestsCnt: Int, twin:Boolean, bathroom:String, level: Int, options: Seq[String])
object Room{
  def fromXml(n: Node):Room=
    Room(n \@"num" toInt,
      n \@"guests" toInt,
      n \@"addguests" toInt,
      n \@"twin" toBoolean,
      n \@"bathroom",
      (n \@"level").toInt,
      (n \ "option").map(_.text))

  implicit val rw = Json.writes[Room]

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
    (n \@"lcoprice").toDouble * 100 toLong)

  implicit val tw = Json.writes[Tariff]
}


case class Prices(room: Long, bkf: Long, eci: Long, lco: Long)

object Prices{
  implicit val pm = Monoid[Prices]
  implicit val pw = Json.writes[Prices]
}

case class Category (id: String, name:String, rooms: Seq[Room], tariffs: Seq[Tariff], prices: Prices){
  def maxGuestCnt(roomCnt: Int) = rooms.sortBy(_.guestsCnt)(Ordering[Int].reverse)(roomCnt - 1).guestsCnt
  def maxRoomCnt (guestCnt: Int) = rooms.filter(_.guestsCnt >= guestCnt).size
}

object Category {

  def fromXml(n: Node, from: LocalDateTime, to: LocalDateTime): String \/ Category = {

    def align(tariffs: Seq[Tariff]): String \/ Seq[Tariff] = {

      val filtered = tariffs.sortBy(_.startDate)
        .dropWhile(_.endDate.compareTo(from) < 0)
        .takeWhile(_.startDate.compareTo(to) <= 0)

      if (filtered.isEmpty) s"no active tariffs in category $this".left
      else if (filtered.head.startDate.compareTo(from) > 0 || filtered.last.endDate.compareTo(to) < 0){
        s"""tariffs are not covering date interval: first tariff start date :${tariffs.head.startDate},
            |last tariff end date:${tariffs.last.endDate}.Provided dates: $from:, $to.
        |""".stripMargin.left
      }
      else {
        filtered.sliding(2, 1).find {
          case Seq(p, n) => p.endDate != n.startDate
          case Seq() => false
        }.<\/ {
          val t = (filtered.head.copy(startDate = from) +: filtered.tail)
          t.init :+ t.last.copy(endDate = to)
        }.leftMap(s => s"gap or overlap between tariffs: 1)${s(0)}, 2)${s(1)}")
      }
    }

    val catId = n \@ "id"

    align(n \ "tarif" map (Tariff.fromXml))
    .bimap (
      err => s"categoryId=$catId:$err",

      tariffs => {

        val prices = tariffs.map { tariff =>
          val dif = ChronoUnit.DAYS.between(tariff.startDate, tariff.endDate) + 1
          Prices(tariff.roomPrice * dif, tariff.bkf * dif, tariff.eci * dif, tariff.lco * dif)
        }.reduce(_ |+| _)

        Category(
          catId,
          n \@ "name",
          n \ "room" map (Room.fromXml),
          tariffs,
          prices
        )
      }
    )

  }
  implicit val cw = Json.writes[Category]
}

  case class Hotel(id: String, name: String, checkInTime: LocalTime, checkOutTime: LocalTime, categories: Seq[Category])

  object Hotel {

    def fromXml(n: Node, from: LocalDateTime, to: LocalDateTime): ValidationNel[String, Seq[Hotel]] = {

      (n \ "hotel").map { hotelNode =>

        val hotelId = hotelNode \@ "id"

        (hotelNode \ "category").map(catNode=>
          Category.fromXml(catNode, from, to)
          .validationNel
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


    implicit val hw = Json.writes[Hotel]

  }


