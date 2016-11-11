package ecat.dal

import java.math.BigInteger
import java.time.LocalDateTime

import async.client.ObmenSaitPortType
import ecat.dal.BlockDal._
import otelsait.FIO
import otelsait.Spiszakaz
import otelsait.Zakaz
import play.api.libs.json._

import scala.collection.JavaConverters._
import scala.concurrent.Future
import play.api.libs.functional.syntax._
import ecat.util.DateTime.pertrovichDateTimeFormatter.format

import scala.concurrent.ExecutionContext

object BlockDal {

  type Order = Zakaz
  type OrderList = Spiszakaz
  type UserContactInfo = FIO

  //  case class OrderItem(idHotel: String, idCategory: String, kvOsn: Int, kvDop: Int, twin: Boolean, bkf: Boolean, kvoNumber: Int)
  //  implicit val oiReads = Json.reads[OrderItem]
  implicit val zakazREads: Reads[Order] = {
    ((__ \ "hotelId").read[String] and
      (__ \ "categoryId").read[String] and
      (__ \ "tarifId").read[String] and
      (__ \ "guestsCnt").read[Long] and
      (__ \ "addGuestsCnt").read[Long] and
      (__ \ "twin").read[Boolean] and
      (__ \ "bkf").read[Boolean])((hotelId, catId, idTarif, guestsCnt, addGuestsCnt, twin, bkf) => {
      val z = new Zakaz()
      z.setIdHotell(hotelId)
      z.setIdCategory(catId)
      z.setIdTarif(idTarif)
      z.setKvoOsn(BigInteger.valueOf(guestsCnt))
      z.setKvoDop(BigInteger.valueOf(addGuestsCnt))
      z.setTwin(twin)
      z.setBracvest(bkf)
      z.setKvoNumber(BigInteger.valueOf(1))
      z
    })
  }

  implicit val spisReads = Reads[OrderList](_.validate[List[Zakaz]].map(l=> new Spiszakaz(){spis = l.asJava}))
  //TODO: add email and tel validation
  implicit val userContactInfoReads: Reads[UserContactInfo] = {
    ((__ \ "name").read[String] and
      (__ \ "2ndName").read[String] and
      (__ \ "3rdName").read[String] and
      (__ \ "tel").read[String] and
      (__ \ "email").read[String])((name, _2ndName, _3rdName, tel, email) => {
      val f = new FIO()
      f.setName(name)
      f.setSurname(_2ndName)
      f.setOtchestvo(_3rdName)
      f.setTel(tel)
      f.setEmail(email)
      f
    })
  }
}

class BlockDal(proxy: ObmenSaitPortType) {

  def blockCategory(
    startDate: LocalDateTime,
    endDate: LocalDateTime,
    ttl: BigInteger,
    sum: Float,
    details: OrderList
  )(implicit ec: ExecutionContext): Future[String] = {
    Future(proxy.blockCategory(format(startDate), format(endDate), ttl, details, sum)).map(_.getIdzakaza)
  }

  def blockPay(id: String, uci: UserContactInfo)(implicit ec: ExecutionContext): Future[BigInteger] =  {
    Future(proxy.blockPay(id, uci)).map(_.getTimer)
  }

  def paid(id: String)(implicit ec: ExecutionContext): Future[Boolean] = {
    Future(proxy.paid(id))
  }

}
