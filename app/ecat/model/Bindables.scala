package ecat.model

import java.text.{ParsePosition, FieldPosition, DateFormat}
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

import play.api.mvc.PathBindable
import play.api.mvc.PathBindable.Parsing

/**
 * Created by admin on 10/12/15.
 */
object Bindables {
  val  formatter = DateTimeFormatter.ofPattern("ddMMyy");

  implicit val ldf = new Parsing[LocalDate](LocalDate.parse(_,formatter),formatter.format(_), _ +":"+ _.getMessage)

}