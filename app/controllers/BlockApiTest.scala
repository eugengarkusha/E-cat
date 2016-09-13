//package controllers
//
//
//import java.math.BigInteger
//import java.time.LocalDateTime
//import ecat.util.DateTime.pertrovichDateTimeFormatter.format
//import scala.collection.JavaConverters._
//import async.client.ObmenSaitPortType
//import otelsait.Spiszakaz
//import otelsait.Zakaz
//import play.api.libs.json._
//import play.api.mvc.Action
//import play.api.mvc.BodyParsers
//import play.api.mvc.Controller
//import play.api.libs.functional.syntax._
//
//import scala.concurrent.ExecutionContext.Implicits.global
//import scala.concurrent.Future
//
////TODO: param names are freaky but correspond to  wsdl interface definition. Ask mr. P to rename params appropriately(and change Summa type to Double)
//class BlockApiTest(proxy: ObmenSaitPortType) extends Controller {
//
//  def blockCancel(id: String) = Action.async {
//    Future(proxy.blockCancel(id)).map(x => Ok(x.toString))
//  }
//
//  def blockContinue(id: String) = Action.async {
//    Future(proxy.blockContinue(id)).map(x => Ok(s"tip = ${x.isTip}, timer = ${x.getTimer}"))
//  }
//
//  def blockPay(id: String) = Action.async {
//    Future(proxy.blockPay(id)).map(x => Ok(s"tip = ${x.isTip}, timer = ${x.getTimer}"))
//  }
//
//  def paid(id: String) = Action.async {
//    Future(proxy.paid(id)).map(b => Ok(b.toString))
//  }
//
//  def zayavka(id: String) = Action.async {
//    Future(proxy.zayvka(id)).map(b => Ok(b.toString))
//  }
//
////  case class OrderItem(idHotel: String, idCategory: String, kvOsn: Int, kvDop: Int, twin: Boolean, bkf: Boolean, kvoNumber: Int)
////  implicit val oiReads = Json.reads[OrderItem]
//  implicit val zakazREads: Reads[Zakaz] = {
//    ((__ \ "idHotel").read[String] and
//    (__ \ "idCategory").read[String] and
//    (__ \ "idTarif").read[String] and
//    (__ \ "kvoOsn").read[Long] and
//    (__ \ "kvoDop").read[Long] and
//    (__ \ "twin").read[Boolean] and
//    (__ \ "bkf").read[Boolean] and
//    (__ \  "kvoNumber").read[Long])((hotelId, catId, idTarif, guestsCnt, addGuestsCnt, twin, bkf, roomCnt) => {
//      val z = new Zakaz()
//      z.setIdHotell(hotelId)
//      z.setIdCategory(catId)
//      z.setIdTarif(idTarif)
//      z.setKvoOsn(BigInteger.valueOf(guestsCnt))
//      z.setKvoDop(BigInteger.valueOf(addGuestsCnt))
//      z.setTwin(twin)
//      z.setBracvest(bkf)
//      z.setKvoNumber(BigInteger.valueOf(roomCnt))
//      z
//    })
//  }
//
//  implicit val spisReads = Reads[Spiszakaz](_.validate[List[Zakaz]].map(l=> new Spiszakaz(){spis = l.asJava}))
//
//  def blockCategory(DatN: LocalDateTime, DatK: LocalDateTime, TimeBlock: BigInteger, Summa: Float, FIO: String) =
//    Action.async(BodyParsers.parse.json[Spiszakaz]) {req =>
//      Future(proxy.blockCategory(format(DatN), format(DatK), TimeBlock, req.body, Summa, FIO: String))
//      .map(r => Ok(s"tip = ${r.isTip}, idZakaza = ${r.getIdzakaza}, timer = ${r.getTimer}"))
//    }
//
//
//
//}
