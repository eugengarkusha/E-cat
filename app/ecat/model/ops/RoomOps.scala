package ecat.model.ops

import shapeless.record.Record
import ecat.model.Schema.Room
import scala.xml.Node

object RoomOps {

  def fromXml(n: Node): Room = {

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
