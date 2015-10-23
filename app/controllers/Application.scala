package controllers

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.xml.ws.BindingProvider

import async.client.ObmenSait
import play.api._
import play.api.mvc._

class Application extends Controller {


 //TODO: delete
//  def index = Action {
//    Ok(views.html.index("Your new application is ready."))
//  }


  val proxy ={
    val srv = new ObmenSait().getObmenSaitSoap()
    val req_ctx =  srv.asInstanceOf[BindingProvider].getRequestContext
    req_ctx.put(BindingProvider.USERNAME_PROPERTY, "sait");
    req_ctx.put(BindingProvider.PASSWORD_PROPERTY, "sait555");
    srv
  }

  private val  df = DateTimeFormatter.ofPattern("yyyyMMdd");

  //TODO: think of correct name for this.
  //return the rendered booking template here
  def bookingCategories(from: LocalDate,to: LocalDate) = Action {
    //Just forwarding XML from 1C
    val hhmmss = "000000"
    val _from = df.format(from) + hhmmss
    val _to = df.format(to) + hhmmss
    val s = proxy.getNomSvobod(_from, _to)
    Ok(s"Success from =$from, to=$to\n$s")

  }

}
