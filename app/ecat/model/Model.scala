package ecat.model

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, LocalTime, LocalDate}
import java.util.Date

import scala.xml.{Node, Elem}

/**
 * Created by admin on 10/12/15.
 */

// TODO: think of handling parsing errors(maybe in controller).

object Formatters{
  val  dateFormatter = DateTimeFormatter.ofPattern("ddMMyy");
  val  pertrovichDateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddhhmmss");
}

object Room{

  def fromXml(n: Node):Room=
    Room(n \@"nom" toInt,
        n \@"kvoosn" toInt,
        n \@"kvodop" toInt,
        n \@"twin" toBoolean,
        n \@"bathroom",
        n \@"level",
        (n \ "option").map(_.text))

}
//options - additional details about number setup. They may become the dynamic part of filters .
case class Room(number:Int, guestsCnt: Int, additionalGuestsCnt: Int, twin:Boolean, bathroom:String, level:String, options: Seq[String])




object Tariff{
  def fromXml(n: Node):Tariff = ???
}
case class Tariff(id: String,startDate: LocalDate, endDate: LocalDate, price:Double, breakfastPrice: Double, earlyСheckInPrice: Double, lateCHeckOutPrice: Double)





object Category{
  def fromXml(n: Node):Category =
    Category(n \@"id",
             n \@"name",
             n \"room" map(Room.fromXml),
             n \ "tariff" map(Tariff.fromXml))


}
case class Category (id: String, name:String, numbers: Seq[Room], tariffs: Seq[Tariff])






object Hotel{

  def fromXml(n: Node)=
    Hotel(n \@"id",
          n \@"name",
          LocalTime.of(n \@"timein" toInt, 0),
          LocalTime.of(n\@"timeout" toInt, 0),
          n \ "categotiy" map(Category.fromXml))

}

case class Hotel(id:String, name:String, checkInTime:LocalTime, checkOutTime:LocalTime, categories: Seq[Category])
