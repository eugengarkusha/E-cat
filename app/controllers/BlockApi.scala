package controllers


import java.math.BigInteger
import java.time.LocalDateTime
import ecat.dal.BlockDal
import ecat.dal.BlockDal._
import otelsait.FIO
import otelsait.Spiszakaz
import otelsait.Zakaz
import play.api.libs.json._
import play.api.mvc.Action
import play.api.mvc.BodyParsers
import play.api.mvc.Controller
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

//TODO: param names are freaky but correspond to  wsdl interface definition. Ask mr. P to rename params appropriately
class BlockApi(dal: BlockDal)(implicit ec: ExecutionContext) extends Controller {

//  def blockCancel(id: String) = Action.async {
//    Future(proxy.blockCancel(id)).map(x => Ok(x.toString))
//  }
//
//  def blockContinue(id: String) = Action.async {
//    Future(proxy.blockContinue(id)).map(x => Ok(s"tip = ${x.isTip}, timer = ${x.getTimer}"))
//  }
//


//  def zayavka(id: String) = Action.async {
//    Future(proxy.zayvka(id)).map(b => Ok(b.toString))
//  }

//use this for posting:
//Action.async(BodyParsers.parse.json[Spiszakaz]) {req =>
  def blockCategory(startDate: LocalDateTime, endDate: LocalDateTime, ttl: BigInteger, sum: Float, details: OrderList) = {
    Action.async(dal.blockCategory(startDate, endDate, ttl, sum, details).map(Ok(_)))
  }

  def blockPay(id: String, uci: UserContactInfo) = Action.async {
    dal.blockPay(id, uci).map(t=>Ok(JsNumber(t.longValueExact())))
  }

  def paid(id: String) = Action.async {
    dal.paid(id).map(b=> Ok(JsBoolean(b)))
  }






}
