package ecat.model.ajax

import java.time.LocalTime

import ecat.model.{CategoryOps, Filters, Prices, Schema}
import Schema._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import schema.RecordFilters.Filter
import schema.heplers.Materializer.materialize
import ecat.util.DateTime._
import shapeless._
import record._
import CategoryOps._
import ecat.util.JsonFormats._

object CategoryControlProtocol {

 //no defaults intentionally(let protocol be consistent)
  case class CatCtrlRequest(hotelId: String,
                            catId: String,
                            guestsCnt: Int,
                            tariffsHash: Int,
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

    def calcGroupPrice (p: Prices, hotelCiTime:LocalTime, hotelCoTime:LocalTime): Double= {

      def addIf(cond: Boolean, l: Long, r:Long) = if (cond) l + r else r
      (roomCnt * addIf(lco, p.get('lco), addIf(eci, p.get('eci), addIf(bkf, p.get('bkf), p.get('room))))).toDouble / 100
    }

    //TODO: consider rewriting in terms of filtering AST when it is implemented(in would also cover filtering by roomCnt)
    val result  = for{
      h<- hotels.filter(_.get('id) == hotelId)
      c<- h.get('categories).filter(_.get('id) == catId)
    }yield {

      if(c.get('tariffGroups).hashCode != tariffsHash){
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
                tg.get('name) -> calcGroupPrice(tg.get('overalPrices), eci, lco)
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
