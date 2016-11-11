package controllers

import java.time.LocalDateTime

import async.client.ObmenSaitPortType
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import ecat.dal.HotelsDal
import ecat.model.ops.HotelOps
import ecat.model.ops.HotelOps.{isEci, isLco}
import play.api.cache.CacheApi
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext



//for SHow derivation based on contrib Show[HList] instance
import ecat.model.Schema._
import ecat.model.ajax.catctrl.CategoryControlProtocol
import ecat.model.ajax.catctrl.CategoryControlProtocol.{Gone => CatGone}
import ecat.model.ajax.catctrl.CategoryControlProtocol._
import ecat.util.DateTime.interval
import schema.RecordFilters.Filter
import shapeless.contrib.scalaz.instances._

import scalaz.Show
//product writes leads to an issue with Map setialization(conflicts with writes of traversable of tuples wich are products and writable by productwrites)
import schema.RecordJsonFormats.{productWrites => _, _}
import shapeless._
import views.html.pages.tariffs
import views.html.pages.{category => cat}
//RecordOps
import record._
import scala.concurrent.Future
import scala.concurrent.duration._

//TODO: clean this fucking mess!
class Application (dal: HotelsDal)(implicit ec: ExecutionContext) extends Controller {

  def getDummyOffers(from: LocalDateTime, to: LocalDateTime) = Action.async {  implicit req =>
    dal.getHotels(from,to).map{ hotels=>
//    val s = implicitly[Show[Hotel]]
//    println("hotels="+hotels.map(s.shows(_)).mkString("\n","\n","\n"))
    Ok(views.html.pages.offers(hotels, from, to))
    }
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

  //hotelFilter is needed to ensure that only eiligable entities will be passed in CategoryControlProtocol.process
  def category(from: LocalDateTime, to: LocalDateTime, ctrl: CatCtrlRequest, hotelFilter: Filter[Hotel]) = Action.async{req =>

    val hotelsFut = dal.getHotels(from, to).map(_.flatMap(hotelFilter(_)))
    // putting inside a function to have an access to req. TODO: implement updateWith on coproducts!!
    hotelsFut.map { hotels =>
      val h = hotels.headOption
      object resp2Js extends Poly1 {
        implicit def _ctrl = at[CtrlResponse](ctrl => Json.obj("type" -> "basic", "ctrl" -> ctrl))
        implicit def _tariffs = {
          at[TariffsRedraw](tr => Json.obj(
            "type" -> "tariffsRedraw",
            "ctrl" -> tr.get('ctrl),
            "html" -> tariffs(tr.get('tg), isEci(from.toLocalTime, h.get), isLco(to.toLocalTime, h.get)).toString)
          )
        }
        implicit def full = {
          at[FullRedraw](fr => Json.obj(
            "type" -> "fullRedraw",
            "html" -> cat(fr.get('category),
            fr.get('hotel),isEci(from.toLocalTime, h.get), isLco(to.toLocalTime, h.get))(req).toString)
          )
        }
        implicit def gone = at[CatGone.type](tr => Json.obj("type" -> "gone"))
      }

      Ok(CategoryControlProtocol.process(ctrl, hotels).map(resp2Js).unify)
    }

  }

//
  def filter(from: LocalDateTime, to: LocalDateTime, filter: Filter[Hotel]) = Action.async{ implicit req =>

    dal.getHotels(from, to).map{ hotels =>
      Ok(views.html.pages.offers(hotels.flatMap(filter(_)),from, to))
    }
  }



}
