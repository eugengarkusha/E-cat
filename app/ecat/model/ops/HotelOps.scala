package ecat.model.ops

import java.time.{LocalDateTime, LocalTime}

import shapeless.record.Record
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
      def cats:ValidationNel[String, List[Category]] = {
        (hotelNode \ "category").toList
          .map(c=>CategoryOps.fromXml(c, from, to).map(_ :: Nil))
          .reduce(_ +++ _)
          .leftMap(errs => NonEmptyList(s"hotelId=$id:$errs"))
      }
      cats.map(c => Record(id = id, name = name, checkInTime = ci, checkOutTime = co , categories = c) :: Nil)

    }.reduce(_ +++ _)
  }


}
