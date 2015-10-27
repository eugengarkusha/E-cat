package ecat.model

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, LocalTime}
import Formatters.{pertrovichDateTimeFormatter => petrovichDtf}

import scala.xml.{Node, Elem}

/**
 * Created by admin on 10/12/15.
 */

// TODO: think of handling parsing errors(maybe in controller).
//TODO: thing of good place for formatters
object Formatters{
  val  dateFormatter = DateTimeFormatter.ofPattern("ddMMyy");
  val  pertrovichDateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
}


object Hotel{

  def fromXml(n: Node):Seq[Hotel] = {
    //println("converting n"+ n.toString)
    (n \ "hotel").map(n=>Hotel(n \@"id",
      n \@"name",
      LocalTime.of(n \@ "ckeckin" toInt, 0),
      LocalTime.of(n \@ "checkout" toInt, 0),
      n \ "category" map(Category.fromXml)))}

}

case class Hotel(id:String, name:String, checkInTime:LocalTime, checkOutTime:LocalTime, categories: Seq[Category])


object Category{
  def fromXml(n: Node):Category =
    Category(n \@"id",
             n \@"name",
             n \"room" map(Room.fromXml),
             n \ "tarif" map(Tariff.fromXml))


}
case class Category (id: String, name:String, numbers: Seq[Room], tariffs: Seq[Tariff])


object Tariff{
  def fromXml(n: Node):Tariff = Tariff(
    n \@"id" ,
    n \@"name" ,
    LocalDateTime.parse(n \@"dateN", petrovichDtf) ,
    LocalDateTime.parse(n \@"dateK", petrovichDtf) ,
    (n \@"roomprice").toDouble,
    (n \@"bkfprice").toDouble,
    (n \@"eciprice").toDouble,
    (n \@"lcoprice").toDouble)
}
case class Tariff(id: String,name:String, startDate: LocalDateTime, endDate: LocalDateTime, room: Double, bkf: Double, eci: Double, lco: Double)


object Room{

  def fromXml(n: Node):Room=
    Room(n \@"num" toInt,
      n \@"guests" toInt,
      n \@"addguests" toInt,
      n \@"twin" toBoolean,
      n \@"bathroom",
      (n \@"level").toInt,
      (n \ "option").map(_.text))

}
//options - additional details about number setup. They may become the dynamic part of filters .
case class Room(number:Int, guestsCnt: Int, additionalGuestsCnt: Int, twin:Boolean, bathroom:String, level: Int, options: Seq[String])




