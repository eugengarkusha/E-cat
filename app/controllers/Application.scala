package controllers

import java.time.{LocalDateTime, LocalTime, ZoneOffset}
import javax.xml.ws.BindingProvider

import async.client.ObmenSait
import ecat.model.{Hotel, _}
import play.api.cache.CacheApi
import play.api.libs.json.{JsArray, JsObject, Json}
import play.api.mvc._
import ecat.util.DateTime.localDateTimeOrdering
import schema.RecordJsonFormats._
import ecat.util.JsonFormats._

import scala.concurrent.duration._
import ecat.util.DateTime.{pertrovichDateTimeFormatter => fmt}

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


    def lbl = "H:"+interval(from, to)
//    def hotels = Json.parse(scala.io.Source.fromFile(env.getFile("conf/json")).mkString).as[Seq[Hotel]]
    def  load = Future(fetchData(from,to)).map { s =>
      val h = Hotel.fromXml(scala.xml.XML.loadString(s), from, to.minusDays(1)).fold(err => throw new Exception(err.toString), identity)
      cache.set(lbl,h, 3.minutes)
      println("h="+h)
      h
    }
//
//    cache.get[Seq[Hotel]](lbl).map(Future.successful(_)).getOrElse(load)
    load
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
    getHotels(LocalDateTime.of(2016,5,5,22,32,28),LocalDateTime.of(2016,6,30,0,0,0)).map{r=>
      Ok(r.toString)
    }

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
//
//
//

//Category code in unrelated to reality
//  def category(from: LocalDateTime, to: LocalDateTime, ctrl: CategoryCtrl) = Action.async{req =>
//    getHotels(from, to).map { hotels =>
//    val resp =  Ok{
//
//      filter(hotels).map{
//        case Right(c)=>{
//
//         val trfs =  cat.get('tariffs)
//         val grpd:Map[String,Tariff] = trfs.groupBy(_.get('name))//.sortBy(_.get('startDate)) noneed to sort
//         val base = grpd.getOrElse("Проживание", throw new Exception("no base tariff deteced(by name Проживание)"))
//
//
//
////          tariffs ++
//            Json.obj(
//              "changed" -> false,
//              "price" -> ctrl.price(filteredC.prices),
//              "maxGuestCnt" -> cat.maxGuestCnt(filteredC.roomCnt),
//              "maxRoomCnt" -> cat.maxRoomCnt(filteredC.guestsCnt)
//            )
//        }
//        case Left(c)=>{
//          Json.obj(
//            "changed" -> true,
//            "categoryHtml" -> views.html.pages.category.render(c, h.id, req).toString
//          )
//        }
//      }.getOrElse(Json.obj("changed" -> true, "categoryHtml" -> ""))
//
//
//
//    }
//  }
//
  def filter(from: LocalDateTime, to: LocalDateTime, filter: Filter[Schema.Hotel]) = Action.async{ implicit req =>
    getHotels(from, to).map(hotels => Ok(views.html.pages.offers(hotels.flatMap(filter))))
  }



}
