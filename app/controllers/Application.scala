package controllers

import java.time.{LocalDate, LocalDateTime, LocalTime, ZoneOffset}
import javax.xml.ws.BindingProvider

import async.client.ObmenSait
import ecat.model.ops.HotelOps
import play.api.cache.CacheApi
import play.api.libs.json.{JsArray, JsObject, Json}
import play.api.mvc._
import schema.RecordJsonFormats._
import ecat.util.JsonFormats._
import ecat.model.ajax.CategoryControlProtocol
import CategoryControlProtocol.{Gone=>_Gone,_}
import views.html.pages.{category=>cat,_}
import scala.concurrent.duration._
import shapeless._
import record._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import ecat.util.DateTime.interval
import schema.RecordFilters.Filter
import ecat.model.Schema._
import ecat.model.ajax.CategoryControlProtocol.CatCtrlRequest
//TODO: clean this fucking mess!
class Application (cache: CacheApi, env: play.api.Environment ) extends Controller {

  //replace with real proxy call
  private def getHotels(from: LocalDateTime, to: LocalDateTime): Future[Seq[Hotel]] ={

    val (_fakeFrom, _fakeTo) = (LocalDateTime.of(2016,5,6,0,0,0),LocalDateTime.of(2016,6,30,0,0,0))//(from,to)//


    def lbl = "H:"+interval(_fakeFrom, _fakeTo)

    def  load = Future(fetchData(_fakeFrom,_fakeTo)).map { s =>
      val h = HotelOps.fromXml(scala.xml.XML.loadString(s), _fakeFrom, _fakeTo).fold(err => throw new Exception(err.toString), identity)
      cache.set(lbl,h, 40.minutes)
//      println("h="+h)
      h
    }
//
    cache.get[Seq[Hotel]](lbl).map(Future.successful(_)).getOrElse(load)
//    load
//    Future.successful(hotels)
  }

  private def fetchData(from: LocalDateTime, to: LocalDateTime):String= {//(String, String, String)={
    val emptyXml = "<empty></empty>"
//   val middle = proxy.getNomSvobod(fmt.format(from), fmt.format(to))
//   val left  = if(from.toLocalDate == LocalDate.now) emptyXml else proxy.getNomSvobod(fmt.format(from.minusDays(1)), fmt.format(from))
//   val right = proxy.getNomSvobod(fmt.format(to), fmt.format(to.plusDays(1)))

    val middle = scala.io.Source.fromFile(env.getFile("conf/xml20160505223228_20160630000000"))(scala.io.Codec.UTF8).mkString
//    (emptyXml, middle, emptyXml)
    middle
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

    getHotels(from, to).map { hotels =>

      val resp = CategoryControlProtocol.process(ctrl, hotels.flatMap(hotelFilter(_))) match {

        case ctrl:CtrlResponse =>Json.obj("type" -> "basic", "ctrl"->ctrl)

        case TariffsRedraw(ctrl, tgrps) =>Json.obj("type" -> "tariffsRedraw", "ctrl"->ctrl, "html"-> "")//tariffGroups.render(tgrps).toString)

        case FullRedraw(h, c)=> Json.obj("type" -> "fullRedraw", "html"-> cat.render(c, h, req).toString)

        case _Gone => Json.obj("type" -> "gone")
      }

      Ok(resp)
    }
  }

//
  def filter(from: LocalDateTime, to: LocalDateTime, filter: Filter[Hotel]) = Action.async{ implicit req =>

    getHotels(from, to).map{ hotels =>
      val k = hotels.flatMap(filter(_))

       Ok(views.html.pages.offers(hotels.flatMap(filter(_))))
    }
  }



}
