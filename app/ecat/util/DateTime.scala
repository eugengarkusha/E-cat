package ecat.util

import java.time._, format.DateTimeFormatter


object DateTime {

  val  dateFormatter = DateTimeFormatter.ofPattern("ddMMyy")
  val  pertrovichDateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

  implicit val localDateTimeOrdering = Ordering.fromLessThan[LocalDateTime](_.compareTo(_) < 0)
}
