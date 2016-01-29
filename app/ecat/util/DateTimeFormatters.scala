package ecat.util

import java.time.format.DateTimeFormatter

object DateTimeFormatters {
  val  dateFormatter = DateTimeFormatter.ofPattern("ddMMyy");
  val  pertrovichDateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
}
