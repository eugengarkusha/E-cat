package ecat.model

import ecat.util.DateTime.{pertrovichDateTimeFormatter => fmt}
import java.time.LocalDateTime

import ecat.model.ajax.Mappings._
import play.api.libs.json.Json
import play.api.mvc.PathBindable.{Parsing=>PParsing}
import play.api.mvc.QueryStringBindable.{Parsing=>QParsing}

/**
 * Created by admin on 10/12/15.
 */
object Bindables {

  implicit val localDate = new PParsing[LocalDateTime](LocalDateTime.parse(_, fmt), fmt.format(_), _ +":"+ _.getMessage)
  implicit val categoryCtrl = new QParsing[CategoryCtrl](Json.parse(_).as, Json.toJson(_).toString, _ +":"+ _.getMessage)

}