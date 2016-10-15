package ecat.model.ajax.catctrl

import ecat.model.Filters._
import ecat.model.Schema._
import ecat.model.ops.PricesOps._
import play.api.libs.json._
import schema.RecordFilters.Filter
import shapeless._
import shapeless.record._
import java.time.LocalTime
import ecat.model.ops.HotelOps.{isEci, isLco}

import shapeless.Coproduct.MkCoproduct

object CategoryControlProtocol {

  type RoomCtrlRequest = Record.`'id -> Int,'guestsCnt -> Int,'addGuestsCnt -> Int,'twin -> Boolean,'bkf -> Boolean`.T

  //TODO: rename tariffGroupsHash to tariffsHash
  type CatCtrlRequest = Record.`'hotelId -> String,'catId -> String, 'roomReqs -> List[RoomCtrlRequest], 'tariffGroupsHash -> Int, 'ci -> LocalTime,'co -> LocalTime`.T

  //shapeless has a bug :" Malformed literal or standard type (Map[String" if map is put inline.(fails also with shapeless 2.3.1)TODO: fix in shapeless
  type prices = Map[String, Double]
  type RoomLimits = Record.`'guestsCnt -> Int, 'addGuestsCnt -> Int, 'twin -> Boolean`.T
  type RoomCtrlResponse = Record.`'id -> Int, 'limits -> RoomLimits, 'prices -> prices`.T
  type CtrlResponse = Record.`'maxRoomCnt -> Int, 'roomCtrls -> List[RoomCtrlResponse]`.T
  type TariffsRedraw = Record.`'ctrl -> CtrlResponse, 'tg -> List[Tariff]`.T
  //TODO: Hotel contains all needed info, remove category
  type FullRedraw = Record.`'hotel -> Hotel, 'category -> Category`.T
  case object Gone

  type Response = CtrlResponse :+: TariffsRedraw :+: FullRedraw :+: Gone.type :+: CNil

  val wrapCp = Coproduct[Response]

  def process(req: CatCtrlRequest, hotels: Seq[Hotel]): Response = {

    val hotelId :: catId :: roomReqs :: tariffsHash :: ci :: co :: HNil = req

    def filterByIds(hotelId: String, catId: String, hotels: Seq[Hotel]): Option[Hotel] = {
      hotels.find(_.get('id) == hotelId)
      .map(_.updateWith('categories)(_.filter(_.get('id) == catId)))
      .filter(_.get('categories).nonEmpty)
    }

    def getLims(rooms: List[Room]): Option[Map[Int, RoomLimits]] = {
      def toInt(b: Boolean) = if (b) 1 else 0

      MiscFunctions.limits(
        data = rooms.map(r => List(r.get('guestsCnt), r.get('additionalGuestsCnt), toInt(r.get('twin)))),
        input = roomReqs.map(rr => rr.get('id) -> List(rr.get('guestsCnt), rr.get('addGuestsCnt), toInt(rr.get('twin))))(collection.breakOut)
      ).map(_.map { case (id, List(gc, addGc, _twin)) => id -> Record(guestsCnt = gc, addGuestsCnt = addGc, twin = _twin > 0) })
    }

    def mkControlResponse(availRoomsCnt: Int, tariffs: List[Tariff], roomReqs: List[RoomCtrlRequest], lims: Map[Int, RoomLimits], hotel: Hotel): CtrlResponse = {

      def roomCtrlResps: List[RoomCtrlResponse] = roomReqs.map { rr =>
        Record(
          id = rr.get('id),
          limits = lims(rr.get('id)),
          prices = tariffs.map { t =>
            def price = calcPrice(t.get('overallPrices), rr.get('guestsCnt), rr.get('addGuestsCnt), rr.get('bkf), rr.get('twin), isEci(ci, hotel), isLco(co, hotel))
            t.get('name) -> price
          }.toMap
        )
      }

      Record(maxRoomCnt = availRoomsCnt, roomCtrls = roomCtrlResps)
    }



    filterByIds(hotelId, catId, hotels) match {
      case None =>
        println(s"category(id = $catId) has gone during booking process.")
        wrapCp(Gone)

      case Some(hotel) =>{
        assert(hotel.get('categories).size == 1)
        val category = hotel.get('categories).head
        val tariffs = category.get('tariffs)
        val rooms = category.get('rooms)

        getLims(rooms) match {

          case None =>
            println(s"category has changed during booking process.Redrawing category: $catId")
            //TODO: Hotel contains all needed info, remove category
            wrapCp(Record(hotel = hotel, category = hotel.get('categories).head))

          case Some(lims) if tariffs.hashCode != req.get('tariffGroupsHash) =>
            println(s"Tariffs has changed during booking process. Redrawing Tariffs: $catId")
            wrapCp(Record(ctrl = mkControlResponse(rooms.size, tariffs, roomReqs, lims, hotel), tg = tariffs))

          case Some(lims) => wrapCp(mkControlResponse(rooms.size, tariffs, roomReqs, lims, hotel))

        }
      }
    }

  }
}
