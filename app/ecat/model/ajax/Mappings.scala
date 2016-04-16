package ecat.model.ajax

import play.api.libs.json._
import play.api.libs.functional.syntax._
object Mappings {

  case class CategoryCtrl(hotelId: String,
                          catId: String,
                          hash: Int,
                          guestsCnt: Int,
                          roomCnt: Int,
                          bkf: Boolean,
                          eci: Boolean,
                          lco: Boolean)

   implicit val catReads: Reads[CategoryCtrl]  = {
    ((__ \ "hotelId").read[Int] ~
    (__ \ "catId").read[Int] ~
    (__ \ "hash").read[Int] ~
    (__ \ "guestsCnt").read[Int].orElse(Reads(_ => JsSuccess(1))) ~
    (__ \ "roomCnt").read[Int].orElse(Reads(_ => JsSuccess(1)))~
    (__ \ "bkf").read[Boolean].orElse(Reads(_ => JsSuccess(false)))~
    (__ \ "eci").read[Boolean].orElse(Reads(_ => JsSuccess(false)))~
    (__ \ "lco").read[Boolean].orElse(Reads(_ => JsSuccess(false))))(CategoryCtrl.apply _)
  }
  implicit val catWrites: Writes[CategoryCtrl] = Json.writes[CategoryCtrl]
}
