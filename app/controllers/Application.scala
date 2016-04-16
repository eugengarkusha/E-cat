package controllers

import java.time.{LocalDate, LocalDateTime, LocalTime, ZoneOffset}
import javax.xml.ws.BindingProvider

import async.client.ObmenSait
import com.google.inject.Inject
import ecat.model.Category.Prices
import ecat.model.ajax.Mappings.CategoryCtrl
import ecat.model.{Category, Hotel, Room, Tariff}
import play.api.cache.CacheApi
import play.api.libs.json.{JsArray, JsObject, Json}
import play.api.mvc._

import scala.concurrent.duration._
import ecat.util.DateTime.{pertrovichDateTimeFormatter => fmt}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.NodeSeq

class Application (cache: CacheApi) extends Controller {


  private def interval(from: LocalDateTime, to: LocalDateTime): Long = {
    to.toEpochSecond(ZoneOffset.UTC) - from.toEpochSecond(ZoneOffset.UTC)
  }
  //replace with real proxy call
  private def getHotels(from: LocalDateTime, to: LocalDateTime): Seq[Hotel] ={
    val h = Seq{
      val tariff = Tariff("tarif_id", "tariff_name", LocalDateTime.now(), LocalDateTime.now().plusDays(20), 10, 2, 2, 2)
      val room = Room(1, 2, 1, true, "wtf", 3, Seq("with a smell of a homless", "partially flooded"))
      val cat = Category("cat_id", "SweetCategory", Seq(room, room.copy(number = 3, twin = false, options = Nil)), Seq(tariff))
      Hotel("some_id", "Ekaterina", LocalTime.NOON, LocalTime.MIDNIGHT, Seq(cat, cat.copy(id = "id2", name = "ShitCategory")))
    }

    cache.getOrElse("H:"+interval(from, to), 5.minutes)(h)
  }

  def getPrices(from: LocalDateTime, to: LocalDateTime, cat: Category) = {
    cache.getOrElse[Category.Prices](s"P:${interval(from, to)}:${cat.hashCode()}", 30.minutes)(cat.prices(from, to))
  }

// TODO: create and maintain proxy outside the controller
  val proxy ={
    val srv = new ObmenSait().getObmenSaitSoap()
    val req_ctx =  srv.asInstanceOf[BindingProvider].getRequestContext
    req_ctx.put(BindingProvider.USERNAME_PROPERTY, "sait");
    req_ctx.put(BindingProvider.PASSWORD_PROPERTY, "sait555");
    srv
  }

  def dumpXml(from:String, to:String)= Action.async{
      //Just forwarding XML from 1C
      Future(proxy.getNomSvobod(from, to)).map(Ok(_))
  }

  //TBD: parse dates on rcv(format://ГГГГММДДЧЧММСС)
  def getAvailableRooms(from:String, to:String)= Action.async{
    Future(proxy.getNomSvobod(from, to)).map{s=>
      val hotels = Hotel.fromXml(scala.xml.XML.loadString(s))
      //cache.set()
      Ok(Json.toJson(hotels))
    }
  }

  def getDummyOffers(from: LocalDateTime, to: LocalDateTime) = Action {  implicit req =>
    Ok(views.html.pages.offers(getHotels(from,to)))
  }

  def main = Action{
    Ok(views.html.index())
  }
  def ekaterina = Action{
    Ok(views.html.pages.hotels.ekaterina.ekaterina())
  }
  def ekaterina2 = Action{
    Ok(views.html.pages.hotels.ekaterina2.ekaterina2())
  }
  def gallery = Action{
    Ok(views.html.pages.gallery())
  }
  def blog = Action{
    Ok(views.html.pages.blog())
  }
  def news = Action{
    Ok(views.html.pages.news())
  }
  def promo = Action{
    Ok(views.html.pages.promo())
  }
  def about = Action{
    Ok(views.html.pages.about())
  }
  def contacts = Action{
    Ok(views.html.pages.contacts())
  }
  def comment = Action{
    Ok(views.html.pages.comment())
  }
  def post = Action{
    Ok(views.html.pages.blog_pages.post())
  }
  def event = Action{
    Ok(views.html.pages.news_pages.event())
  }

  def reservation(hotel: String, from:String, to:String) = Action{implicit  req =>
    Ok(views.html.pages.reservation(from, to))
  }

  // def cat  = Action{ implicit req =>
  //   Ok(views.html.pages.category(getHotels(LocalDateTime.now(),LocalDateTime.now()).head.categories.head))
  // }



  def category(from: LocalDateTime, to: LocalDateTime, ctrl: CategoryCtrl) = Action{req =>

    val resp = for{
      h   <- getHotels(from, to).find(_.id == ctrl.hotelId)
      cat <- h.categories.find(_.id == ctrl.catId)
    } yield{

      def redraw = Json.obj(
          "changed"-> true,
          "categoryHtml"-> views.html.pages.category.render(cat, h.id, req).toString
      )

      if(cat.hashCode == ctrl.hash) {
        try {
          Json.obj(
            "changed" -> false,
            "price" -> getPrices(from, to, cat).price(ctrl.roomCnt, ctrl.bkf, ctrl.eci, ctrl.lco),
            "maxGuestCnt" -> cat.maxGuestCnt(ctrl.roomCnt),
            "maxRoomCnt" -> cat.maxRoomCnt(ctrl.guestsCnt)
          )
        }catch{
          //todo: handling situation where hash matched but cats are not equal(redraw category and log errors)
          //case t:Throwable => report(t); redraw
          //lets look if we encounter these situations in development
          case t:Throwable => throw t
        }
      }else redraw

    }

    Ok(resp.getOrElse(Json.obj("changed" -> true, "categoryHtml" -> "")))

  }

  def filter(from: LocalDateTime, to: LocalDateTime, hotelFilters: JsObject,roomFilters: JsObject, roomOptFilters:JsArray) = Action{implicit  req =>
    val filtered = ecat.model.Filters(getHotels(from, to),hotelFilters: JsObject,roomFilters: JsObject, roomOptFilters:JsArray)
    Ok(views.html.pages.offers(filtered.fold(errs => throw new Exception(errs.toString()), identity )))
  }



}
