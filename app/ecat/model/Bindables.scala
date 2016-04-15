package ecat.model

import ecat.util.DateTime.{pertrovichDateTimeFormatter => fmt}
import java.time.LocalDateTime

import ecat.model.ajax.Mappings._
import play.api.libs.json.{JsArray, JsObject, Json}
import play.api.mvc.PathBindable.{Parsing => PParsing}
import play.api.mvc.QueryStringBindable.{Parsing => QParsing}

/**
 * Created by admin on 10/12/15.
 */
object Bindables {

  implicit val localDate = new PParsing[LocalDateTime](LocalDateTime.parse(_, fmt), fmt.format(_), _ +":"+ _.getMessage)
  implicit val categoryCtrl = new QParsing[CategoryCtrl](Json.parse(_).as, Json.toJson(_).toString, _ +":"+ _.getMessage)
  implicit val jsObject = new QParsing[JsObject](Json.parse(_).as[JsObject], _.toString, _ +":"+ _.getMessage)
  implicit val jsArray = new QParsing[JsArray](Json.parse(_).as[JsArray], _.toString, _ +":"+ _.getMessage)

}