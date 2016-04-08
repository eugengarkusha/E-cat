package ecat.util

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import play.api.libs.json.{JsNumber, JsResult, _}
import play.api.libs.json.Writes._
import play.api.libs.json.Writes.TemporalFormatter._
object JsonFormats {

  implicit val localTimeFormat = new Format[LocalTime]{
    override def reads(j: JsValue): JsResult[LocalTime] = j.validate[JsNumber].map(bd=>LocalTime.ofSecondOfDay(bd.value.toLong))

    override def writes(o: LocalTime): JsValue = JsNumber(o.toSecondOfDay)
  }
}
