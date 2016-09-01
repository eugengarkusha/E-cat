package controllers

import java.math.BigInteger

import async.client.ObmenSaitPortType
import otelsait.Zakaz
import play.api.libs.json.JsString
import play.api.libs.json.Json
import play.api.libs.json.Writes
import play.api.mvc.Action
import play.api.mvc.Controller

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

//TODO: param names are freaky but correspond to  wsdl interface definition. Ask mr. P to rename params appropriately(and change Summa type to Double)
class BlockApiTest(proxy: ObmenSaitPortType) extends Controller {


  def blockCancel(id: String)= Action.async {
    Future(proxy.blockCancel(id)).map(x => Ok(x.toString))
  }

  case class BlockCancelResp(tip: Boolean, id: String, timer: BigInteger)
  implicit val bigIntegerWrites =  Writes[BigInteger](bi => JsString(bi.toString))
  implicit val blockCancelRespWrites = Json.writes[BlockCancelResp]
  //need Number to be a list of Zakaz
  def blockCategory(DatN: String, DatK: String, idHotell: String,idCategory: String, idGost: String , TimeBlock: BigInteger, Number: Zakaz, Summa: Float) = Action.async{
    Future(proxy.blockCategory(DatN, DatK, idHotell, idCategory, idGost, TimeBlock, Number, Summa))
    .map(r=> Ok(Json.toJson(BlockCancelResp(r.isTip, r.getIdzakaza, r.getTimer))))
  }

}
