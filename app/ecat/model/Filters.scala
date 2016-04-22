package ecat.model

import java.time.{LocalDateTime, LocalTime}
import ecat.util.JsonFormats.localTimeFormat
import play.api.libs.json._
import shapeless._
import labelled._
import record._
import scalaz.{Lens => _, _}
import Scalaz._

//todo: filters are too couples with JsObjects, is it ok?
object Filters {

  type PureFilter[T, V] = (T, V)=> Boolean
  type Filter[T] = T => Boolean
  type JsFilter[T] = JsValue => String \/ (T => Boolean)


  private def applyFilters(hf: Filter[Hotel], rf: Filter[Room], of: Filter[String], hotels: Seq[Hotel])= {

    def filterByNonEmpty[T,V[X]<:Seq[X],X](lens:Lens[T,V[X]])(s:Seq[T])(tFilter:V[X]=>V[X]):Seq[T]={
      s.iterator.map(t=> lens.set(t)(tFilter(lens.get(t)))).filter(lens.get(_).nonEmpty).toSeq
    }

    filterByNonEmpty(lens[Hotel] >> 'categories)(hotels.filter(hf)) { cats =>
      filterByNonEmpty(lens[Category] >> 'rooms)(cats)(_.filter(rf)) /*=>
        filterByNonEmpty(lens[Room] >> 'options)(rooms.filter(rf))(_.filter(of))
      }*/
    }
  }


  private def filter[V,T](get: T => V)(cmp: (V,V)=>Boolean)(t:T, v:V): Boolean = cmp(get(t),v)

  private def eqFilter[V,T](get: T => V)(r:T,t:V): Boolean = filter(get)(_ == _)(r,t)

  private def combine[T](fs: Seq[Filter[T]]): Filter[T] = (t:T) => fs.foldLeft(true)((agr, n) => agr && n(t))

  private def setupFilters[T](settings: collection.Map[String, JsValue], jsFilters: collection.Map[String, JsFilter[T]]): ValidationNel[String, Filter[T]] = {
    if(settings.isEmpty)((_:T)=>true).successNel
    else{
      settings.map{case (k,v)=>
        jsFilters.get(k).\/>(s"filter '$k not found").flatMap(_(v)).validationNel.map(_ :: Nil)
      }.reduce(_ +++ _).map(combine[T])
    }
  }

  private val roomOptPureFilter = eqFilter[String,String](identity) _

  private val roomPureFilters = {
    def r[V] = eqFilter[V,Room] _
    Record(
      twin = r(_.twin),
      addGuests = r(_.additionalGuestsCnt),
      guests = filter[Int, Room](_.guestsCnt)(_ >= _) _,
      bath = r(_.bathroom)
    )
  }

  private val hotelPureFilters = {
    def r[V] = eqFilter[V,Hotel] _
    Record(
      ci = r(_.checkInTime),
      co = r(_.checkOutTime),
      name = r(_.name)
    )
  }

  private object toParsedFilter extends Poly1{
    implicit def caseT[K<:Symbol,V,T](implicit r :Reads[V], w:Witness.Aux[K]) = at[FieldType[K,PureFilter[T,V]]](pureFilter =>
      w.value.name -> ((jv: JsValue)=> \/.fromEither(r.reads(jv).map(v => (t:T) => pureFilter(t, v)).asEither).leftMap(_.map(_._2).mkString))
    )
  }

  //todo: think of better names:
  private val hoteJslFilters:Map[String, JsFilter[Hotel]] = hotelPureFilters.map(toParsedFilter).toList.toMap
  private val roomJsFilters:Map[String, JsFilter[Room]] = roomPureFilters.map(toParsedFilter).toList.toMap

  def apply(hotels: Seq[Hotel], hotelFilterSettings: JsObject, roomFilterSettings: JsObject, roomOptFilterSettings:JsArray): ValidationNel[String, Seq[Hotel]] = {
    val hotelFilters = setupFilters(hotelFilterSettings.value, hoteJslFilters)
    val roomFilters = setupFilters(roomFilterSettings.value, roomJsFilters)
    val roomOptFilters = combine[String](roomOptFilterSettings.value.map(f=>roomOptPureFilter.curried(f.toString)))
    (hotelFilters|@|roomFilters)(applyFilters(_, _, roomOptFilters, hotels))
  }

/*
  //Usage example:

  //settings from FE
  val roomFilterSettings = Map("twin"->JsBoolean(true),"guests"->JsNumber(2),"bath"->JsString("wtf"))
  val hotelFilterSettings = Map("ci"->JsNumber(LocalTime.NOON.toSecondOfDay),"name"->JsString("Ekaterina"))
  val roomOptFilterSettings = List("with a smell of a homless")

  //Data
  val tariff = Tariff("tarif_id","tariff_name", LocalDateTime.now(), LocalDateTime.now().plusDays(20), 10, 2, 2, 2)
  val room = Room(1,2,1,true,"wtf",3,Seq("with a smell of a homless","partially flooded"))
  val cat = Category("cat_id","cat_name",Seq(room,room.copy(number = 3,twin = false,options = Nil)), Seq(tariff))
  val h = Hotel("some_id","Ekaterina", LocalTime.NOON,LocalTime.MIDNIGHT,Seq(cat, cat.copy(id="id2")))


  val a = setupFilters(hotelFilterSettings, hoteJslFilters)
  val b = setupFilters(roomFilterSettings, roomJsFilters)
  val c = combine[String](roomOptFilterSettings.map(roomOptPureFilter.curried(_)))

  //ValidationNel[String, List[Hotel]]
  (a|@|b)(applyFilters(_, _, c, Seq(h)))

*/

}
