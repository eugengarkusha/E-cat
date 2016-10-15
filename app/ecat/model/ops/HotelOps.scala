package ecat.model.ops

import java.time.LocalDate
import java.time.LocalTime
import shapeless.record._
import ecat.model.Schema._
import scalaz.syntax.bind.ToBindOps
import scala.xml.Node
import scalaz.{Category => _, _}
import Scalaz._
import ValidationOps._catch

object HotelOps {

  def fromXml(n: Node, from: LocalDate, to: LocalDate): \/[String, List[Hotel]] = {

    _catch("exception while parsing Hotels payload") {
      (n \ "hotel").toList.traverseU{ hotelNode =>

        val id = hotelNode \@ "id"
        val name = hotelNode \@ "name"
        val ci = LocalTime.of(hotelNode \@ "ckeckin" toInt, 0)
        val co = LocalTime.of(hotelNode \@ "checkout" toInt, 0)
        val eci = LocalTime.of(hotelNode \@ "eci" toInt, 0)
        val lco = LocalTime.of(hotelNode \@ "lco" toInt, 0)
        val catNodes = (hotelNode \ "category").toList

        (if (catNodes.isEmpty) -\/(s"No categories in hotel name = $name id = $id") else \/-(catNodes))
        .flatMap(_.traverseU(CategoryOps.fromXml(_, from, to)))
        .bimap(
          errs => s"hotelId = $id: $errs",
          cats => Record(id = id, name = name, checkInTime = ci, checkOutTime = co, eci = eci, lco = lco, categories = cats)
        )

      }
    }.join

  }

  def maxRoomCnt(hotels: Seq[Hotel]): Int = hotels.iterator.map(_.get('categories).iterator.map(_.get('rooms).size).max).max

  def isEci(t: LocalTime, hotel: Hotel) = t.compareTo(hotel.get('checkInTime)) < 0 && t.compareTo(hotel.get('eci))  >= 0
  def isLco(t: LocalTime, hotel: Hotel) = t.compareTo(hotel.get('checkOutTime)) > 0 && t.compareTo(hotel.get('lco)) <= 0


}
