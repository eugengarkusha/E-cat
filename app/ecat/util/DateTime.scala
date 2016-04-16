package ecat.util

import java.time._, format.DateTimeFormatter


object DateTime {

  val  dateFormatter = DateTimeFormatter.ofPattern("ddMMyy")
  val  pertrovichDateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
  implicit val localDateTimeOrdering = Ordering.fromLessThan[LocalDateTime](_.compareTo(_) < 0)

  def interval(from: LocalDateTime, to: LocalDateTime): Long = {
    to.toEpochSecond(ZoneOffset.UTC) - from.toEpochSecond(ZoneOffset.UTC)
  }

}
