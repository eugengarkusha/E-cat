package ecat.model.ops

import ecat.model.Schema._
import shapeless.record._

import scala.xml.Node

object PricesOps {

  def fromXml(n: Node): Prices = {
    def toOpt(price: Long) = Some(price).filter(_ >= 0)
    Record(
      room = (n \@ "roomprice").toDouble * 100 toLong,
      bkf = toOpt((n \@ "bkfprice").toDouble * 100 toLong),
      twin = toOpt((n \@ "twinprice").toDouble * 100 toLong),
      eci = (n \@ "eciprice").toDouble * 100 toLong,
      lco = (n \@ "lcoprice").toDouble * 100 toLong
    )
  }

  //TODO: Manage effects!!(get on options will cause exceptions)
  def calcPrice(p: Prices, guestCnt: Int, addGuestsCnt: Int, bkf: Boolean, twin: Boolean, eci: Boolean, lco: Boolean): Double = {

    {
      def pr(cond: Boolean, p: => Long) = if (cond) p else 0

      pr(bkf, (addGuestsCnt + guestCnt) * p.get('bkf).getOrElse(0L)) +
        pr(twin, p.get('twin).getOrElse(0L)) +
        pr(eci, p.get('eci)) +
        pr(lco, p.get('lco)) +
        p.get('room)
    }.toDouble / 100

  }

}
