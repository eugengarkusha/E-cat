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
                            //back to filters
                            //twinRequired: Boolean,
                            bkf: Boolean,
                            ci: LocalTime,
                            co: LocalTime)

  case class CatCtrlResponse(maxGuestCnt:Int, maxAddGuesstsCnt: Int, availableRoomCnt:Int, prices:Map[String,Double],eci:Boolean, lco:Boolean)


  implicit val catCtrlReads = Json.format[CatCtrlRequest]
  implicit val ctrRespWrites = Json.format[CatCtrlResponse]


  //def filterById - filters hotel and category by id - if not foud - not found msg
  //def filterBySettings - filters category  by control - if not found - redraw ms
  //after filters check tariffs hash - if not match - redraw tariffs msg
  //if tariffs hash match  - calc prices

  //Yes, I know, I know. This runtime-omfg-shit will be eliminated after intodustion of Filters AST!!!
  def filterById (hotelId: String,catId: String, hotels: Seq[Hotel]):Option[Hotel] = {
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

  def filterBySettings(guestsCnt: Int, addGuestsCnt: Int, roomCnt:Int, cat: Category):Option[Category]= {
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


  def process(req: CatCtrlRequest, hotels: Seq[Hotel]):Option[Either[(Category, Hotel), CatCtrlResponse]] = {

    import req._

    for {
      h <- filterById(hotelId, catId, hotels)
      c = h.get('categories).head
    } yield {
      //do not redraw category, just reflect tariff changes in prices block
      if (c.get('tariffGroups).hashCode != tariffGroupsHash) {
        println(s"Tariffs has changed during booksbting process. Redrawing category: $catId")
        Left(c -> h)
      }
      else {

        filterBySettings(guestsCnt, addGuestsCnt, roomCnt, c).map { filteredCat =>

          val eci = ci.compareTo(h.get('checkInTime)) < 0
          val lco = co.compareTo(h.get('checkOutTime)) > 0

          def prices: Map[String, Double] = {
            filteredCat.get('tariffGroups)
              .map { tg =>
                tg.get('name) -> roomCnt * calcPrice(tg.get('overalPrices), guestsCnt, addGuestsCnt, bkf, eci, lco)
              }(collection.breakOut)
          }

          Right(
            CatCtrlResponse(
              maxGuestCnt(filteredCat, roomCnt),
              maxAddGuestCnt(filteredCat, roomCnt),
              filteredCat.get('rooms).size,
              prices,
              eci,
              lco
            )
          )
        }.getOrElse {
          println(s"category has changed during booking process.Redrawing category: $catId")
          Left(c -> h)
        }
      }

    }
  }
}
