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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import ecat.util.DateTime.interval

//TODO: clean this fucking mess!
class Application (cache: CacheApi) extends Controller {

  //replace with real proxy call
  private def getHotels(from: LocalDateTime, to: LocalDateTime): Seq[Hotel] ={
    //remove this method(validations are done in Model.scala):
    def validateCategory(c: Category, from: LocalDateTime, to: LocalDateTime) = {

      val tarifs = c.tariffs.sortBy(_.startDate)

      if(tarifs.isEmpty)
        throw new Exception(s"no tariffs in category $this")
      tarifs.reduceLeft { (p, n) =>
        if (p.endDate != n.startDate) throw new Exception(s"gap or overlap between tariffs: 1)$p, 2)$n")
        p
      }
      if(tarifs.head.startDate.compareTo(from) > 0 || tarifs.last.endDate.compareTo(to) < 0)
        throw new Exception(s"tariffs are not covering date interval: first Tariff start date :${tarifs.head.startDate}, last tarif end date:${tarifs.last.endDate}.Provided dates: $from:, $to ")

    }
    val date = LocalDateTime.of(2016,11,11,11,11,11)
    val h = Seq{
      val tariff = Tariff("tarif_id", "tariff_name", date, date.plusDays(1), 10, 2, 2, 2)
      val room = Room(1, 2, 1, true, "wtf", 3, Seq("with a smell of a homless", "partially flooded"))
      val cat = Category(
                    "cat_id",
                   "SweetCategory",
                   Seq(room, room.copy(number = 3, twin = false, options = Nil, guestsCnt = 3)),
                   Seq(tariff,tariff.copy(startDate=date.plusDays(1),endDate = date.plusDays(5))),
                   Prices(10, 3, 5, 4)
                )
      validateCategory(cat,from,to)
      Hotel("some_id", "Ekaterina", LocalTime.NOON, LocalTime.MIDNIGHT, Seq(cat, cat.copy(id = "id2", name = "ShitCategory")))
    }

    cache.getOrElse("H:"+interval(from, to), 5.minutes)(h)
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
      val hotels = Hotel.fromXml(scala.xml.XML.loadString(s), from, to).fold(err=> throw new Exception(err.toString), identity)
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
            "price" -> ctrl.price(cat.prices),
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
