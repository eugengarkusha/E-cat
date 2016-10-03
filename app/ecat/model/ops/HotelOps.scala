package ecat.model.ops

import java.time.{LocalDateTime, LocalTime}

import shapeless.record._
import ecat.model.Schema._
import scala.xml.Node
import scalaz.{NonEmptyList, Category=>_,_} ,Scalaz._

object HotelOps {

  def fromXml(n: Node, from: LocalDateTime, to: LocalDateTime): ValidationNel[String, List[Hotel]] = {

    (n \ "hotel").map { hotelNode =>

      val id = hotelNode \@ "id"
      def name = hotelNode \@ "name"
      def ci = LocalTime.of(hotelNode \@ "ckeckin" toInt, 0)
      def co = LocalTime.of(hotelNode \@ "checkout" toInt, 0)
      def eci = LocalTime.of(hotelNode \@ "eci" toInt, 0)
      def lco = LocalTime.of(hotelNode \@ "lco" toInt, 0)
      def cats:ValidationNel[String, List[Category]] = {
        val _cats = (hotelNode \ "category").toList
        if (_cats.isEmpty) s"No categories in hotel name=$name id=$id".failureNel
        else{
          _cats
          .map(c => CategoryOps.fromXml(c, from, to).map(_ :: Nil))
          .reduce(_ +++ _)
          .leftMap(errs => NonEmptyList(s"hotelId=$id:$errs"))
        }
      }
      cats.map(c => Record(id = id, name = name, checkInTime = ci, checkOutTime = co ,eci = eci, lco = lco, categories = c) :: Nil)

    }.foldLeft(List.empty[Hotel].successNel[String])(_ +++ _)
  }

  def maxRoomCnt(hotels: Seq[Hotel]): Int = hotels.iterator.map(_.get('categories).iterator.map(_.get('rooms).size).max).max

  def isEci(t: LocalTime, hotel: Hotel) = t.compareTo(hotel.get('checkInTime)) < 0 && t.compareTo(hotel.get('eci)) > 0
  def isLco(t: LocalTime, hotel: Hotel) = t.compareTo(hotel.get('checkOutTime)) > 0 && t.compareTo(hotel.get('lco)) < 0


}
