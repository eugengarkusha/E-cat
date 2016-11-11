package controllers

import async.client.ObmenSaitPortType
import play.api.mvc.Action
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionException
import scala.concurrent.Future

class Debug(proxy: ObmenSaitPortType)(implicit ec: ExecutionContext) extends Controller {

  def dumpXml(from:String, to:String) = Action.async{
    Future(proxy.getNomSvobod(from, to)).map(Ok(_))
  }


  def testPayment = Action{req =>
    println("reqbody="+req.body)
    println("reqbodyFormUrl="+req.body.asFormUrlEncoded)
    println("reqbodyMultipart="+req.body.asMultipartFormData)
    println("reqbodyText="+req.body.asText)

    Ok("whoops")
  }

  def tst = Action{

    Ok(views.html.tst.render())
  }

}
