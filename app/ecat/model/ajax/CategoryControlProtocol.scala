package ecat.model.ajax

import java.time.LocalTime

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

object CategoryControlProtocol {

 //no defaults intentionally(let protocol be consistent)
  case class CatCtrlRequest(hotelId: String,
                            catId: String,
                            guestsCnt: Int,
                            addGuestsCnt:Int,
                            tariffGroupsHash: Int,
                            roomCnt: Int,
                            bkf: Boolean,
                            ci: LocalTime,
                            co: LocalTime)


  sealed trait Response
  case class CtrlResponse(maxGuestCnt:Int, maxAddGuesstsCnt: Int, availableRoomCnt:Int, eci:Boolean, lco:Boolean, prices: Map[String, Double])extends Response
  case class TariffsRedraw(ctrl: CtrlResponse, tg:List[TariffGroup])extends Response
  case class FullRedraw(hotel: Hotel, category: Category)extends Response
  case object Gone extends Response

  implicit val catCtrlReads = Json.format[CatCtrlRequest]
  implicit val ctrlWrites = Json.format[CtrlResponse]

  //Yes, I know, I know. This runtime-omfg-shit will be eliminated after intodustion of Filters AST!!!
  private def filterByIds (hotelId: String,catId: String, hotels: Seq[Hotel]):Option[Hotel] = {
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

  private def filterBySettings(guestsCnt: Int, addGuestsCnt: Int, roomCnt:Int, cat: Category):Option[Category]= {
    val f = Json.obj(
      "rooms"->Json.obj(
        "SIZE"->roomCnt,
        "elFilter"->Json.obj(
         "guestsCnt"->Json.obj("op"->"GTEQ","v"->guestsCnt),
          "addGuestsCnt" ->Json.obj("op"->"GTEQ","v"->addGuestsCnt)
        )
      )
    ).as[Filter[Category]]
   f(cat)
  }


  def process(req: CatCtrlRequest, hotels: Seq[Hotel]): Response = {

    import req._

    filterByIds(hotelId, catId, hotels).map{hotel=>

      filterBySettings(guestsCnt, addGuestsCnt, roomCnt, hotel.get('categories).head).map { filteredCat =>

        val eci = ci.compareTo(hotel.get('checkInTime)) < 0
        val lco = co.compareTo(hotel.get('checkOutTime)) > 0
        val tariffGroups = filteredCat.get('tariffGroups)

        def ctrl = CtrlResponse(
          maxGuestCnt(filteredCat, roomCnt),
          maxAddGuestCnt(filteredCat, roomCnt),
          filteredCat.get('rooms).size,
          eci,
          lco,
          prices = tariffGroups.map { tg =>
              tg.get('name) -> roomCnt * calcPrice(tg.get('overalPrices), guestsCnt, addGuestsCnt, bkf, eci, lco)
          }(collection.breakOut)
        )

        if (tariffGroups.hashCode == tariffGroupsHash) ctrl
        else {
          println(s"Tariffs has changed during booksbting process. Redrawing Tariffs: $catId")
          TariffsRedraw(ctrl, tariffGroups)
        }
      }.getOrElse {
        println(s"category has changed during booking process.Redrawing category: $catId")
        FullRedraw(hotel, hotel.get('categories).head)
      }
    }.getOrElse(Gone)
  }
}
