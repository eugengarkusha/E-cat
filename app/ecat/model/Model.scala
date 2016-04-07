package ecat.model

import java.time.{LocalDateTime, LocalTime}
import ecat.util.DateTimeFormatters.{pertrovichDateTimeFormatter => fmt, _}
import play.api.libs.json.Json
import ecat.util.JsonFormats._
import scala.xml.Node
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


case class Tariff(id: String, name:String, startDate: LocalDateTime, endDate: LocalDateTime, roomPrice: Double, bkf: Double, eci: Double, lco: Double)

object Tariff{
  def fromXml(n: Node):Tariff = Tariff(
    n \@"id" ,
    n \@"name" ,
    LocalDateTime.parse(n \@"dateN", fmt) ,
    LocalDateTime.parse(n \@"dateK", fmt) ,
    (n \@"roomprice").toDouble,
    (n \@"bkfprice").toDouble,
    (n \@"eciprice").toDouble,
    (n \@"lcoprice").toDouble)

  implicit val tw = Json.writes[Tariff]
}


case class Category (id: String, name:String, numbers: Seq[Room], tariffs: Seq[Tariff])

object Category{
  def fromXml(n: Node):Category =
    Category(n \@"id",
      n \@"name",
      n \"room" map(Room.fromXml),
      n \ "tarif" map(Tariff.fromXml))

  implicit val cw = Json.writes[Category]

}


case class Hotel(id:String, name:String, checkInTime:LocalTime, checkOutTime:LocalTime, categories: Seq[Category])

object Hotel{

  def fromXml(n: Node):Seq[Hotel] = {
    //println("converting n"+ n.toString)
    (n \ "hotel").map(n=>Hotel(n \@"id",
      n \@"name",
      LocalTime.of(n \@ "ckeckin" toInt, 0),
      LocalTime.of(n \@ "checkout" toInt, 0),
      n \ "category" map(Category.fromXml)))}

  implicit val hw = Json.writes[Hotel]


}


