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

  case class CatCtrlResponse(maxGuestCnt:Int, availableRoomCnt:Int, prices:Map[String,Double])


  implicit val catCtrlReads = Json.format[CatCtrlRequest]
  implicit val ctrRespWrites = Json.format[CatCtrlResponse]




  def process(req: CatCtrlRequest, hotels: Seq[Hotel]):Option[Either[Category, CatCtrlResponse]] = {

    import req._

    def calcGroupPrice (p: Prices, hotelCiTime:LocalTime, hotelCoTime:LocalTime): Double= {
      def eci = ci.compareTo(hotelCiTime) < 0
      def lco = co.compareTo(hotelCoTime) > 0
      def addIf(cond: Boolean, l: Long, r:Long) = if (cond) l + r else r
      (roomCnt * addIf(lco, p.get('lco), addIf(eci, p.get('eci), addIf(bkf, p.get('bkf), p.get('room))))).toDouble / 100
    }

    //TODO: consider rewriting in terms of filtering AST when it is implemented(in would also cover filtering by roomCnt)
    val preFiltered: Seq[(Category, Hotel)] = for{
      h<- hotels.filter(_.get('id) == hotelId)
      c<- h.get('categories).filter(_.get('id) == catId)
    }yield c->h

    assert(preFiltered.size > 1)

    preFiltered.headOption.map { case(category, hotel) =>
      if(category.get('tariffGroups).hashCode != tariffsHash){
        println(s"Tariffs has changed during booking process. Redrawing category: $catId")
        Left(category)
      }
      else {

        val filteredCat = category.updateWith('rooms)(_.filter(r => r.get('guestsCnt) >= guestsCnt && (!twinRequired || r.get('twin))))

        if (filteredCat.get('rooms).size < roomCnt) {
          println(s"category has changed during booking process.Redrawing category: $catId")
          Left(category)
        }
        else {

          def prices: Map[String, Double] = {
            filteredCat.get('tariffGroups)
            .map{ tg =>
               tg.get('name) -> calcGroupPrice(tg.get('overalPrices), hotel.get('checkInTime), hotel.get('checkOutTime))
            }(collection.breakOut)
          }

          Right(CatCtrlResponse (maxGuestCnt(filteredCat,roomCnt), availableRoomCnt(filteredCat, guestsCnt),prices))

        }
      }
    }
  }
}
