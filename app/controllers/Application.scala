package controllers

import java.time.{LocalDate, LocalDateTime, LocalTime, ZoneOffset}
import javax.xml.ws.BindingProvider

import async.client.ObmenSait
import com.typesafe.config.ConfigFactory
import ecat.model.ops.HotelOps
import ecat.util.DateTime
import play.api.cache.CacheApi
import play.api.libs.json._
import play.api.mvc._
//product writes has leads to an issue with Map setialization(conflicts with writes of traversable of tuples wich are products and writable by productwrites)
import schema.RecordJsonFormats.{productWrites=>_,_}
import ecat.util.JsonFormats._
import views.html.pages.{tariffs, category => cat, _}

import scala.concurrent.duration._
import shapeless._
import record._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import ecat.util.DateTime.{pertrovichDateTimeFormatter=>fmt,interval}
import schema.RecordFilters.Filter
import ecat.model.Schema._
import ecat.model.ajax.catctrl.CategoryControlProtocol
import CategoryControlProtocol.{Gone=>CatGone,_}

//TODO: clean this fucking mess!
class Application (cache: CacheApi, env: play.api.Environment ) extends Controller {

  val conf = ConfigFactory.defaultApplication()

  //replace with real proxy call
  private def getHotels(from: LocalDateTime, to: LocalDateTime): Future[Seq[Hotel]] ={

    val (_from, _to) = {
      if(conf.getBoolean("fakedata"))(LocalDateTime.of(2016,5,12,0,0,0),LocalDateTime.of(2016,6,12,0,0,0))
      else from->to
    }


    def lbl = "H:"+interval(_from, _to)


    def  load = Future(fetchData(_from,_to)).map { s =>
      HotelOps.fromXml(scala.xml.XML.loadString(s), _from, _to).fold(err => throw new Exception(err.toString), identity)
    }
//
    if(conf.getBoolean("cache.enabled")) {
      cache.get[Seq[Hotel]](lbl).map(Future.successful(_)).getOrElse {
        val hotels = load
        cache.set(lbl, hotels, conf.getInt("cache.ttl").minutes)
        hotels
      }
    }
    else load
  }

  private def fetchData(from: LocalDateTime, to: LocalDateTime):String= {
    if(conf.getBoolean("fakedata")) {
      scala.io.Source.fromFile(env.getFile("conf/xml20160512000000_20160612000000"))(scala.io.Codec.UTF8).mkString
    }else  proxy.getNomSvobod(fmt.format(from), fmt.format(to))
  }

// TODO: create and maintain proxy outside the controller
  val proxy ={
    val srv = new ObmenSait().getObmenSaitSoap()
    val req_ctx =  srv.asInstanceOf[BindingProvider].getRequestContext
    req_ctx.put(BindingProvider.USERNAME_PROPERTY, "sait");
    req_ctx.put(BindingProvider.PASSWORD_PROPERTY, "sait555");
    srv
  }

  def block(r: JsObject) = Action{
    Ok("true")
  }

  def dumpXml(from:String, to:String)= Action.async{
      //Just forwarding XML from 1C
      Future(proxy.getNomSvobod(from, to)).map(Ok(_))
  }

  def tst= Action.async{
    getHotels(null,null).map(r=>Ok(r.toString))

  }

  def getDummyOffers(from: LocalDateTime, to: LocalDateTime) = Action.async {  implicit req =>
    getHotels(from,to).map(hotels=>Ok(views.html.pages.offers(hotels)))
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


  def category(from: LocalDateTime, to: LocalDateTime, ctrl: CatCtrlRequest, hotelFilter: Filter[Hotel]) = Action.async{req =>
    // putting inside a function to have an access to req. TODO: implement updateWith on coproducts!!
     object resp2Js extends Poly1{
      implicit def _ctrl = at[CtrlResponse](ctrl=>Json.obj("type" -> "basic", "ctrl"->ctrl))
      implicit def _tariffs = at[TariffsRedraw](tr=>Json.obj("type" -> "tariffsRedraw", "ctrl"->tr.get('ctrl), "html"-> tariffs(tr.get('tg)).toString))
      implicit def full = at[FullRedraw](fr=>Json.obj("type" -> "fullRedraw", "html"->cat(fr.get('category),fr.get('hotel))(req).toString))
      implicit def gone = at[CatGone.type](tr=>Json.obj("type" -> "gone"))
    }

    getHotels(from, to).map { hotels =>Ok(CategoryControlProtocol.process(ctrl, hotels.flatMap(hotelFilter(_))).map(resp2Js).unify)}
  }

//
  def filter(from: LocalDateTime, to: LocalDateTime, filter: Filter[Hotel]) = Action.async{ implicit req =>

    getHotels(from, to).map{ hotels =>
      val k = hotels.flatMap(filter(_))

       Ok(views.html.pages.offers(hotels.flatMap(filter(_))))
    }
  }



}
