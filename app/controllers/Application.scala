package controllers

import java.time.{LocalDateTime, LocalTime, ZoneOffset}
import javax.xml.ws.BindingProvider

import async.client.ObmenSait
import ecat.model.Prices
import ecat.model.ajax.Mappings.CategoryCtrl
import ecat.model.{Category, Hotel, Room, Tariff}
import play.api.cache.CacheApi
import play.api.libs.json.{JsArray, JsObject, Json}
import play.api.mvc._
import ecat.util.DateTime.localDateTimeOrdering

import scala.concurrent.duration._
import ecat.util.DateTime.{pertrovichDateTimeFormatter => fmt}
import ecat.model.Filters
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import ecat.util.DateTime.interval
import play.api.Play

//TODO: clean this fucking mess!
class Application (cache: CacheApi, env: play.api.Environment ) extends Controller {

  //replace with real proxy call
  private def getHotels(from: LocalDateTime, to: LocalDateTime): Future[Seq[Hotel]] ={


    def lbl = "H:"+interval(from, to)
//    def hotels = Json.parse(scala.io.Source.fromFile(env.getFile("conf/json")).mkString).as[Seq[Hotel]]
    def  load = Future(proxy.getNomSvobod(fmt.format(from), fmt.format(to))).map { s =>
      val h = Hotel.fromXml(scala.xml.XML.loadString(s), from, to.minusDays(1)).fold(err => throw new Exception(err.toString), identity)
      cache.set(lbl,h, 3.minutes)
      h
    }

    cache.get[Seq[Hotel]](lbl).map(Future.successful(_)).getOrElse(load)
//    hotels
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

  def getAvailableRooms(from: LocalDateTime, to: LocalDateTime)= Action.async{
    Future(proxy.getNomSvobod(fmt.format(from), fmt.format(to))).map{s=>
      val hotels = Hotel.fromXml(scala.xml.XML.loadString(s), from, to.minusDays(1)).fold(err=> throw new Exception(err.toString), identity)
      //cache.set()
      Ok(Json.toJson(hotels))
    }
  }

  def getDummyOffers(from: LocalDateTime, to: LocalDateTime) = Action.async {  implicit req =>
    getHotels(from,to).map(r=>Ok(views.html.pages.offers(r)))
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

  def reservation(from:String, to:String) = Action{implicit  req =>
    Ok(views.html.pages.reservation(from, to))
  }

  // def cat  = Action{ implicit req =>
  //   Ok(views.html.pages.category(getHotels(LocalDateTime.now(),LocalDateTime.now()).head.categories.head))
  // }



  def category(from: LocalDateTime, to: LocalDateTime, ctrl: CategoryCtrl, hotelFilters: JsObject,roomFilters: JsObject, roomOptFilters:JsArray) = Action.async{req =>
    getHotels(from, to).map { hotels =>
      val resp = for {
        h <- Filters(hotels, hotelFilters, roomFilters, roomOptFilters)
            .valueOr(err=>throw new Exception(err.toString))
            .find(_.id == ctrl.hotelId)
        cat <- h.categories.find(_.id == ctrl.catId)
      } yield {

        def redraw = Json.obj(
          "changed" -> true,
          "categoryHtml" -> views.html.pages.category.render(cat, h.id, req).toString
        )

        if (cat.hashCode == ctrl.hash) {
          try {
            Json.obj(
              "changed" -> false,
              "price" -> ctrl.price(cat.prices),
              "maxGuestCnt" -> cat.maxGuestCnt(ctrl.roomCnt),
              "maxRoomCnt" -> cat.maxRoomCnt(ctrl.guestsCnt)
            )
          } catch {
            //todo: handling situation where hash matched but cats are not equal(redraw category and log errors)
            //case t:Throwable => report(t); redraw
            //lets look if we encounter these situations in development
            case t: Throwable => throw t
          }
        } else redraw

      }

      Ok(resp.getOrElse(Json.obj("changed" -> true, "categoryHtml" -> "")))
    }
  }

  def filter(from: LocalDateTime, to: LocalDateTime, hotelFilters: JsObject,roomFilters: JsObject, roomOptFilters:JsArray) = Action.async{implicit  req =>
    getHotels(from, to).map { hotels =>
      val filtered = Filters(hotels, hotelFilters: JsObject, roomFilters: JsObject, roomOptFilters: JsArray)
      Ok(views.html.pages.offers(filtered.fold(errs => throw new Exception(errs.toString()), identity)))
    }
  }



}
