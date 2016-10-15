package ecat.model.ops

import shapeless.record.Record
import ecat.model.Schema.Room
import ValidationOps._catch
import scala.xml.Node
import scalaz.\/

object RoomOps {

  def fromXml(n: Node): \/[String, Room] = _catch("exception while parsing Room payload"){
    Record(
      number = n \@ "num" takeWhile(_.isDigit) toInt,
      guestsCnt = n \@ "guests" toInt,
      additionalGuestsCnt= n \@ "addguests" toInt,
      twin =  n \@ "twin" toBoolean,
      bathroom = n \@ "bathroom",
      level = (n \@ "level").toInt,
      options = (n \ "option").map(_.text).toList
    )
  }

}
