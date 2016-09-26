package ecat.util

import java.time._
import format.DateTimeFormatter

import scalaz.Show


object DateTime {

  val  dateFormatter = DateTimeFormatter.ofPattern("ddMMyy")
  val  pertrovichDateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
  implicit val localDateTimeOrdering = Ordering.fromLessThan[LocalDateTime](_.compareTo(_) < 0)

  def interval(from: LocalDateTime, to: LocalDateTime): Long = {
    to.toEpochSecond(ZoneOffset.UTC) - from.toEpochSecond(ZoneOffset.UTC)
  }

  implicit val localDateShow:Show[LocalTime] = new Show[LocalTime]{
    override def shows(f: LocalTime): String = f.toString
  }

  implicit val localDateTimeShow:Show[LocalDateTime] = new Show[LocalDateTime]{
    override def shows(f: LocalDateTime): String = f.toString
  }

}
