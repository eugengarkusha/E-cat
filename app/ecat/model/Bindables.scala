package ecat.model

import java.math.BigInteger

import ecat.util.DateTime.{pertrovichDateTimeFormatter => fmt}
import java.time.LocalDateTime

import play.api.libs.json.{JsArray, JsObject, Json}
import play.api.mvc.PathBindable.{Parsing => PParsing}
import play.api.mvc.QueryStringBindable.{Parsing => QParsing}
import schema.RecordFilters.Filter
import Filters.hotelFilterReads
import ecat.model.ajax.catctrl.CategoryControlProtocol.CatCtrlRequest
import play.api.mvc.QueryStringBindable
import ecat.util.JsonFormats.localTimeFormat
import schema.RecordJsonFormats.{productReads => _, _}


/**
 * Created by admin on 10/12/15.
 */
object Bindables {

  implicit val localDate = new PParsing[LocalDateTime](LocalDateTime.parse(_, fmt), fmt.format(_), _ +":"+ _.getMessage)
  implicit val bigInteger = new PParsing[BigInteger](new BigInteger(_), _.toString, _ +":"+ _.getMessage)
  implicit val jsObject = new QParsing[JsObject](Json.parse(_).as[JsObject], _.toString, _ +":"+ _.getMessage)
  implicit val jsArray = new QParsing[JsArray](Json.parse(_).as[JsArray], _.toString, _ +":"+ _.getMessage)
  implicit val _filters = new QParsing[Filter[Schema.Hotel]](Json.parse(_).as[Filter[Schema.Hotel]], _.toString, _ +":"+ _.getMessage)
  implicit val categoryCtrl = new QParsing[CatCtrlRequest](Json.parse(_).as[CatCtrlRequest], Json.toJson(_).toString, _ +":"+ _.getMessage)

}