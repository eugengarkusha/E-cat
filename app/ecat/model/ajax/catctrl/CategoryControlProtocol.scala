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
import MiscFunctions._

object CategoryControlProtocol {

  type RoomCtrlRequest = Record.`'id->Int,'guestsCnt-> Int,'addGuestsCnt->Int,'twin->Boolean,'bkf-> Boolean`.T

  type CatCtrlRequest = Record.`'hotelId->String,'catId->String,'roomReqs->List[RoomCtrlRequest],'tariffGroupsHash-> Int, 'ci-> LocalTime,'co-> LocalTime`.T

  //shapeless has a bug :" Malformed literal or standard type (Map[String" if map is put inline.TODO: test with shapeless 2.3.1
  type prices=Map[String, Double]
  type RoomLimits = Record.`'guestsCnt->Int, 'addGuestsCnt-> Int, 'twin->Boolean`.T
  type RoomCtrlResponse = Record.`'id-> Int, 'limits-> RoomLimits, 'prices->prices`.T
  type CtrlResponse = Record.`'eci->Boolean, 'lco->Boolean, 'maxRoomCnt->Int, 'roomCtrls-> List[RoomCtrlResponse]`.T
  type TariffsRedraw = Record.`'ctrl-> CtrlResponse, 'tg->List[TariffGroup]`.T
  type FullRedraw = Record.`'hotel-> Hotel, 'category-> Category`.T
  case object Gone

  type Response = CtrlResponse:+:TariffsRedraw:+:FullRedraw:+:Gone.type:+:CNil
  val respCp = Coproduct[Response]


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



    val hotelId :: catId :: roomReqs:: tariffGroupsHash :: ci :: co :: HNil =  req

    filterByIds(hotelId, catId, hotels).fold[Response]{
      println(s"category(id = $catId) has gone during booking process.")
      respCp(Gone)
    }{hotel=>

      val category =  hotel.get('categories).head
      val rooms = category.get('rooms)

      val limsOpt:Option[Map[Int, RoomLimits]]={

        def toInt(b:Boolean) = if(b) 1 else 0

        MiscFunctions.limits(
          data = rooms.map(r=>List(r.get('guestsCnt),r.get('additionalGuestsCnt),toInt(r.get('twin)))),
          input = roomReqs.map(rr => rr.get('id) -> List(rr.get('guestsCnt),rr.get('addGuestsCnt), toInt(rr.get('twin))))(collection.breakOut)
        ).map(_.map{case (id, lims)=> id->Record(guestsCnt=lims(0),addGuestsCnt=lims(1),twin= lims(2)>0)})
      }

      limsOpt.fold[Response] {
        println(s"category has changed during booking process.Redrawing category: $catId")
        respCp(Record(hotel=hotel, category=hotel.get('categories).head))
      } {lims=>

        val eci = ci.compareTo(hotel.get('checkInTime)) < 0 && ci.compareTo(hotel.get('eci))> 0
        val lco = co.compareTo(hotel.get('checkOutTime)) > 0 && co.compareTo(hotel.get('lco)) < 0

        val tarGrps = category.get('tariffGroups)

        def roomCtrlResps:List[RoomCtrlResponse] = roomReqs.map { rr=>
          Record(
            id = rr.get('id),
            limits = lims(rr.get('id)),
            prices = tarGrps.map{ tg=>
              def price = calcPrice(tg.get('overalPrices), rr.get('guestsCnt), rr.get('addGuestsCnt), rr.get('bkf), rr.get('twin), eci, lco)
              tg.get('name) -> price
            }.toMap
          )
        }

        def resp:CtrlResponse = Record(eci=eci, lco=lco, maxRoomCnt = rooms.size, roomCtrls=roomCtrlResps)

        if (tarGrps.hashCode == tariffGroupsHash) respCp(resp)
        else {
          println(s"Tariffs has changed during booking process. Redrawing Tariffs: $catId")
          respCp(Record(ctrl = resp,tg = tarGrps))
        }
      }
    }
  }
}
