package controllers

import java.time.LocalDate
import javax.xml.ws.BindingProvider
import async.client.ObmenSait
import play.api.mvc._
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

  def dumpXml(from:String, to:String)= Action{
      //Just forwarding XML from 1C
      val s = proxy.getNomSvobod(from, to)
      Ok(s" from =$from, to=$to\n$s")
  }

}
