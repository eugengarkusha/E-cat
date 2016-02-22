package controllers

import java.time.{LocalDateTime, LocalTime, LocalDate}
import javax.xml.ws.BindingProvider
import async.client.ObmenSait
import ecat.model.{Tariff, Room, Category, Hotel}
import play.api.libs.json.Json
import play.api.mvc._
import play.cache.Cache
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Application extends Controller {


// TODO: create and maintain proxy outside the controller
  val proxy ={
    val srv = new ObmenSait().getObmenSaitSoap()
    val req_ctx =  srv.asInstanceOf[BindingProvider].getRequestContext
    req_ctx.put(BindingProvider.USERNAME_PROPERTY, "sait");
    req_ctx.put(BindingProvider.PASSWORD_PROPERTY, "sait555");
    srv
  }

  //TODO: think of correct name for this.
  //return the rendered booking template here
  def bookingCategories(from: LocalDate,to: LocalDate) = Action {
    Ok(s"Success from =$from, to=$to")
  }

  def dumpXml(from:String, to:String)= Action.async{
      //Just forwarding XML from 1C
      Future(proxy.getNomSvobod(from, to)).map(Ok(_))
  }

  //TBD: parse dates on rcv(format://ГГГГММДДЧЧММСС)
  def getAvailableRooms(from:String, to:String)= Action.async{
    Future(proxy.getNomSvobod(from, to)).map{s=>
      val hotels = Hotel.fromXml(scala.xml.XML.loadString(s))
      Ok(Json.toJson(hotels))
    }
  }

  def getDummyJson = Action {
  val tariff = Tariff("tarif_id","tariff_name", LocalDateTime.now(), LocalDateTime.now().plusDays(20), 10, 2, 2, 2)
  val room = Room(1,2,1,true,"wtf",3,Seq("with a smell of a homless","partially flooded"))
  val cat = Category("cat_id","cat_name",Seq(room,room.copy(number = 3,twin = false,options = Nil)), Seq(tariff))
    val h = Hotel("some_id","Ekaterina", LocalTime.NOON,LocalTime.MIDNIGHT,Seq(cat, cat.copy(id="id2")))
    Ok(Json.toJson(h))
  }

}
