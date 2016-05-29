package ecat.model.ajax.catctrl

import ecat.model.{Filters, Schema}
import Schema._
import ecat.model.ops.CategoryOps._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import schema.RecordFilters.Filter
import schema.heplers.Materializer.materialize
import ecat.util.DateTime._
import shapeless._
import record._
import ecat.util.JsonFormats._
import ecat.model.ops.PricesOps._
import ecat.model.Filters._
import java.time.LocalTime
import ecat.model.ajax.catctrl.MiscFunctions
import ecat.model.ajax.catctrl.MiscFunctions._

object CategoryControlProtocol {

 //todo: think on reimplementing with records(1)no need for separate formats 2)following the concept of full case clase substitution)
  case class CatCtrlRequest(hotelId: String,
                            catId: String,
                            roomReqs: List[RoomCtrlRequest],
                            tariffGroupsHash: Int,
                            ci: LocalTime,
                            co: LocalTime)

  case class RoomCtrlRequest(id:Int,
                             guestsCnt: Int,
                             addGuestsCnt:Int,
                             twin:Boolean,
                             bkf: Boolean)


  sealed trait Response
  case class CtrlResponse(eci:Boolean, lco:Boolean, roomCtrls: List[RoomCtrlResponse])extends Response
  case class RoomCtrlResponse(id: Int, limits: RoomLimits, prices: Map[String, Double])
  case class RoomLimits(guestsCnt:Int, addGuestsCnt: Int, twin:Boolean)
  case class TariffsRedraw(ctrl: CtrlResponse, tg:List[TariffGroup])extends Response
  case class FullRedraw(hotel: Hotel, category: Category)extends Response
  case object Gone extends Response


  //Formats
  implicit val roomCtrlReqFormat = Json.format[RoomCtrlRequest]
  implicit val catCtrlReqFormat =  Json.format[CatCtrlRequest]
  implicit val roomLimitsFormat = Json.format[RoomLimits]
  implicit val roomCtrlRespFormat = Json.format[RoomCtrlResponse]
  implicit val ctrlRespFormat = Json.format[CtrlResponse]


  def process(req: CatCtrlRequest, hotels: Seq[Hotel]): Response = {

    //Yes, I know, I know. This runtime-omfg-shit will be eliminated after intodustion of Filters AST!!!
    def filterByIds (hotelId: String,catId: String, hotels: Seq[Hotel]):Option[Hotel] = {
      val f = Json.obj(
        "id"->Json.obj("op"->"EQ","v"->hotelId),
        "categories"->Json.obj(
          "elFilter"->Json.obj(
            "id"->Json.obj("op"->"EQ","v"->catId)
          )
        )
      ).as[Filter[Hotel]]
      hotels.flatMap(f(_)).headOption
    }


    import req._

    filterByIds(hotelId, catId, hotels).fold[Response]{
      println(s"category(id = $catId) has gone during booking process.")
      Gone
    }{hotel=>

      val category =  hotel.get('categories).head
      val rooms = category.get('rooms)


      val limsOpt:Option[Map[Int, RoomLimits]]={

        def toInt(b:Boolean) = if(b) 1 else 0

        MiscFunctions.limits(
          data = rooms.map(r=>List(r.get('guestsCnt),r.get('additionalGuestsCnt),toInt(r.get('twin)))),
          input = req.roomReqs.map(c => c.id -> List(c.guestsCnt,c.addGuestsCnt, toInt(c.twin)))(collection.breakOut)
        ).map(_.map{case (id, lims)=> id->RoomLimits(lims(0),lims(1),lims(2)>0)})
      }

      limsOpt.fold[Response] {
        println(s"category has changed during booking process.Redrawing category: $catId")
        FullRedraw(hotel, hotel.get('categories).head)
      } {lims=>
        val eci = ci.compareTo(hotel.get('checkInTime)) < 0 && ci.compareTo(hotel.get('eci))> 0
        val lco = co.compareTo(hotel.get('checkOutTime)) > 0 && co.compareTo(hotel.get('lco)) < 0

        val tarGrps = category.get('tariffGroups)

        val roomCtrlResps = roomReqs.map { rc=>
          RoomCtrlResponse(
            rc.id,
            limits = lims(rc.id),
            prices = tarGrps.map(tg=>tg.get('name) -> calcPrice(tg.get('overalPrices), rc.guestsCnt, rc.addGuestsCnt, rc.bkf, rc.twin, eci, lco))(collection.breakOut)
          )
        }

        def resp = CtrlResponse(eci,lco,roomCtrlResps)

        if (tarGrps.hashCode == tariffGroupsHash) resp
        else {
          println(s"Tariffs has changed during booksbting process. Redrawing Tariffs: $catId")
          TariffsRedraw(resp, tarGrps)
        }
      }
    }
  }
}
