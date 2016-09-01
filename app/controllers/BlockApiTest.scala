package controllers

import java.math.BigInteger

import async.client.ObmenSaitPortType
import otelsait.Zakaz
import play.api.mvc.Action
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

//TODO: param names are freaky but correspond to  wsdl interface definition. Ask mr. P to rename params appropriately(and change Summa type to Double)
class BlockApiTest(proxy: ObmenSaitPortType) extends Controller {

  def blockCancel(id: String) = Action.async {
    Future(proxy.blockCancel(id)).map(x => Ok(x.toString))
  }

  def blockContinue(id: String) = Action.async {
    Future(proxy.blockContinue(id)).map(x => Ok(s"tip = ${x.isTip}, timer = ${x.getTimer}"))
  }

  def blockPay(id: String) = Action.async {
    Future(proxy.blockPay(id)).map(x => Ok(s"tip = ${x.isTip}, timer = ${x.getTimer}"))
  }

  def paid(id: String) = Action.async {
    Future(proxy.paid(id)).map(b => Ok(b.toString))
  }

  def zayavka(id: String) = Action.async {
    Future(proxy.zayvka(id)).map(b => Ok(b.toString))
  }
  //need Number to be a list of Zakaza(create bindable for it)
  def blockCategory(DatN: String, DatK: String, idHotell: String, idCategory: String, idGost: String, TimeBlock: BigInteger, Number: Zakaz, Summa: Float) =
    Action.async {
      Future(proxy.blockCategory(DatN, DatK, idHotell, idCategory, idGost, TimeBlock, Number, Summa))
      .map(r => Ok(s"tip = ${r.isTip}, idZakaza = ${r.getIdzakaza}, timer = ${r.getTimer}"))
    }


}
