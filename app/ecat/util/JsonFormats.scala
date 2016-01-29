package ecat.util

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import play.api.libs.json.{JsString, Json, Writes}
import play.api.libs.json.Writes._
import play.api.libs.json.Writes.TemporalFormatter._
object JsonFormats {

  implicit val localTimeWrites: Writes[LocalTime] = Writes.apply(t=>JsString(DateTimeFormatter.ISO_LOCAL_TIME.format(t)))
}
