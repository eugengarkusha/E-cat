package controllers

import java.time.{LocalDateTime, LocalTime, ZoneOffset}
import javax.xml.ws.BindingProvider

import async.client.ObmenSait
import ecat.model.ops.HotelOps
import play.api.cache.CacheApi
import play.api.libs.json.{JsArray, JsObject, Json}
import play.api.mvc._
import ecat.util.DateTime.localDateTimeOrdering
import schema.RecordJsonFormats._
import ecat.util.JsonFormats._
import ecat.model.ajax.CategoryControlProtocol, CategoryControlProtocol._
import scala.concurrent.duration._
import ecat.util.DateTime.{pertrovichDateTimeFormatter => fmt}
import shapeless._, record._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import ecat.util.DateTime.interval
import play.api.Play
import schema.RecordFilters.Filter
import ecat.model.Schema._
import ecat.model.ajax.CategoryControlProtocol.CatCtrlRequest
//TODO: clean this fucking mess!
class Application (cache: CacheApi, env: play.api.Environment ) extends Controller {

  //replace with real proxy call
  private def getHotels(from: LocalDateTime, to: LocalDateTime): Future[Seq[Hotel]] ={

    val (_fakeFrom, _fakeTo) = (LocalDateTime.of(2016,5,6,0,0,0),LocalDateTime.of(2016,6,30,0,0,0))


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

  private def fetchData(from: LocalDateTime, to: LocalDateTime):String={
//    proxy.getNomSvobod(fmt.format(from), fmt.format(to))
    scala.io.Source.fromFile(env.getFile("conf/xml20160505223228_20160630000000")).mkString

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


  def category(from: LocalDateTime, to: LocalDateTime, ctrl: CatCtrlRequest ) = Action.async{req =>

    getHotels(from, to).map { hotels =>

      val resp = CategoryControlProtocol.process(ctrl, hotels) match {

        case Some(Right(resp))=>Json.obj("changed" -> false, "ctrl"->resp)

        case Some(Left((category, hotel)))=>{
          Json.obj("changed" -> true, "categoryHtml" -> views.html.pages.category.render(category, hotel, req).toString)
        }
        case None => Json.obj("changed" -> true, "categoryHtml" -> "")
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
