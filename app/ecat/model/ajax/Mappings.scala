package ecat.model.ajax

import ecat.model.Prices
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
                          lco: Boolean){

    def price (p: Prices): Double= {
      def addIf(cond: Boolean, l: Long, r:Long) = if (cond) l + r else r
      addIf(lco, p.lco, addIf(eci, p.eci, addIf(bkf, p.bkf, roomCnt * p.room))).toDouble / 100
    }
  }

   implicit val catReads: Reads[CategoryCtrl]  = {
    ((__ \ "hotelId").read[String] ~
    (__ \ "catId").read[String] ~
    (__ \ "hash").read[Int] ~
    (__ \ "guestsCnt").read[Int].orElse(Reads(_ => JsSuccess(1))) ~
    (__ \ "roomCnt").read[Int].orElse(Reads(_ => JsSuccess(1)))~
    (__ \ "bkf").read[Boolean].orElse(Reads(_ => JsSuccess(false)))~
    (__ \ "eci").read[Boolean].orElse(Reads(_ => JsSuccess(false)))~
    (__ \ "lco").read[Boolean].orElse(Reads(_ => JsSuccess(false))))(CategoryCtrl.apply _)
  }
  implicit val catWrites: Writes[CategoryCtrl] = Json.writes[CategoryCtrl]
}
