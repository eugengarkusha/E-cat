package ecat.util

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import play.api.libs.json.{JsNumber, JsResult, _}
import play.api.libs.json.Writes._
import play.api.libs.json.Writes.TemporalFormatter._
object JsonFormats {

  implicit val localTimeFormat = new Format[LocalTime]{
    override def reads(j: JsValue): JsResult[LocalTime] = j.validate[String].map(bd=>LocalTime.parse(bd))

    override def writes(o: LocalTime): JsValue = JsString(o.toString)
  }

//unneeded
//  val petrovichLocalTimeFormatReads =Reads[LocalTime]{js=>
//    js.validate[String].flatMap{ hourOfDay=>
//      try{JsSuccess(LocalTime.of(hourOfDay.toInt,  0))}
//      catch { case t:Throwable => JsError(s"Cannot parse hourOfDay from $js: ${t.getMessage}")}
//    }
//  }

}
