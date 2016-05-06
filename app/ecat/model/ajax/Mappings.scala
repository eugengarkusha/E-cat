package ecat.model.ajax

import java.time.LocalTime

import ecat.model.{Filters, Prices, Schema}, Schema._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import schema.RecordFilters.Filter
import schema.heplers.Materializer.materialize
import ecat.util.DateTime._
import shapeless._
import record._
import ecat.util.JsonFormats._

object Mappings {

 //no defaults intentionally(let protocol be consistent)
  case class CategoryCtrl(hotelId: String,
                          catId: String,
                          guestsCnt: Int,
                          tariffsHash: Int,
                          roomCnt: Int,
                          twinRequired: Boolean,
                          bkf: Boolean,
                          ci: LocalTime,
                          co: LocalTime){

//    def price (p: Prices): Double= {
//      def addIf(cond: Boolean, l: Long, r:Long) = if (cond) l + r else r
//      (roomCnt * addIf(lco, p.lco, addIf(eci, p.eci, addIf(bkf, p.bkf, p.room)))).toDouble / 100
//    }

//return all needed data instead of category on the right(prices, maxguest/roomCount etc)
    def filter(hotels: Seq[Hotel]):Option[Either[Category,Category]] = {
      //TODO: consider rewriting in terms of filtering AST when it is implemented(in would also cover filtering by roomCnt)
      val preFiltered: Seq[Category] = for{
        h<- hotels.filter(_.get('id) == hotelId)
        c<- h.get('categories).filter(_.get('id) == catId)
      }yield c

      if(preFiltered.size > 1) throw new Exception(s"CategoryCtrl filter returned more then one category: $preFiltered")

      preFiltered.headOption.map { category =>
        if(category.get('tariffGroups).hashCode!=tariffsHash)Left(category)
        else {
          val filtered = category.updateWith('rooms)(_.filter(r => r.get('guestsCnt) >= guestsCnt && (!twinRequired || r.get('twin))))
          if (filtered.get('rooms).size < roomCnt) Left(category) else Right(filtered)
        }
      }

    }

  }



  implicit val catWrites: Writes[CategoryCtrl] = Json.writes[CategoryCtrl]
}
