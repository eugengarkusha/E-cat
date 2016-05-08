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

object CategoryControlProtocol {

 //no defaults intentionally(let protocol be consistent)
  case class CatCtrlRequest(hotelId: String,
                            catId: String,
                            guestsCnt: Int,
                            tariffGroupsHash: Int,
                            roomCnt: Int,
                            twinRequired: Boolean,
                            bkf: Boolean,
                            ci: LocalTime,
                            co: LocalTime)

  case class CatCtrlResponse(maxGuestCnt:Int, availableRoomCnt:Int, prices:Map[String,Double],eci:Boolean, lco:Boolean)


  implicit val catCtrlReads = Json.format[CatCtrlRequest]
  implicit val ctrRespWrites = Json.format[CatCtrlResponse]



  def process(req: CatCtrlRequest, hotels: Seq[Hotel]):Option[Either[(Category, Hotel), CatCtrlResponse]] = {

    import req._

    //TODO: consider rewriting in terms of filtering AST when it is implemented(in would also cover filtering by roomCnt)
    val result  = for{
      h<- hotels.filter(_.get('id) == hotelId)
      c<- h.get('categories).filter(_.get('id) == catId)
    }yield {

      if(c.get('tariffGroups).hashCode != tariffGroupsHash){
        println(s"Tariffs has changed during booking process. Redrawing category: $catId")
        Left(c->h)
      }
      else {

        val filteredCat = c.updateWith('rooms)(_.filter(r => r.get('guestsCnt) >= guestsCnt && (!twinRequired || r.get('twin))))

        if (filteredCat.get('rooms).size < roomCnt) {
          println(s"category has changed during booking process.Redrawing category: $catId")
          Left(c->h)
        }
        else {

          val eci = ci.compareTo(h.get('checkInTime)) < 0
          val lco = co.compareTo(h.get('checkOutTime)) > 0
          def prices: Map[String, Double] = {
            filteredCat.get('tariffGroups)
              .map{ tg =>
                tg.get('name) -> roomCnt * calcPrice(tg.get('overalPrices), bkf, eci, lco)
              }(collection.breakOut)
          }

          Right(CatCtrlResponse (maxGuestCnt(filteredCat,roomCnt), availableRoomCnt(filteredCat, guestsCnt), prices,eci,lco))

        }
      }
    }

    assert(result.size <= 1)

    result.headOption

  }
}
