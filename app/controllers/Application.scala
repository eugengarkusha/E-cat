package controllers

import java.time.{LocalDate, LocalDateTime, LocalTime, ZoneOffset}
import javax.xml.ws.BindingProvider

import async.client.ObmenSait
import com.google.inject.Inject
import ecat.model.{Category, Hotel, Room, Tariff}
import play.api.cache.CacheApi
import play.api.libs.json.Json
import play.api.mvc._
import scala.concurrent.duration._
import ecat.util.DateTimeFormatters.{pertrovichDateTimeFormatter => fmt}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

//TODO: Use compile time DI !!!
class Application @Inject() (cache: CacheApi) extends Controller {


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

  def getDummyOffers(from: LocalDateTime, to: LocalDateTime) = Action {
    def h = {
      val tariff = Tariff("tarif_id", "tariff_name", LocalDateTime.now(), LocalDateTime.now().plusDays(20), 10, 2, 2, 2)
      val room = Room(1, 2, 1, true, "wtf", 3, Seq("with a smell of a homless", "partially flooded"))
      val cat = Category("cat_id", "SweetCategory", Seq(room, room.copy(number = 3, twin = false, options = Nil)), Seq(tariff))
      Hotel("some_id", "Ekaterina", LocalTime.NOON, LocalTime.MIDNIGHT, Seq(cat, cat.copy(id = "id2", name = "ShitCategory")))
    }
    val cacheKey = (to.toEpochSecond(ZoneOffset.UTC) - from.toEpochSecond(ZoneOffset.UTC)).toString
    Ok(views.html.pages.offers(cache.getOrElse(cacheKey, 2.minutes)(Seq(h))))
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

}
