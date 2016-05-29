package ecat.model.ops

import ecat.model.Schema._
import shapeless.record._

import scala.xml.Node

object PricesOps {


  def fromXml(n:Node):Prices={
    Record(
      room = (n \@"roomprice").toDouble * 100 toLong,
      bkf = (n \@"bkfprice").toDouble * 100 toLong,
      twin = (n \@"twinPrice").toDouble * 100 toLong,
      eci = (n \@"eciprice").toDouble * 100 toLong,
      lco = (n \@"lcoprice").toDouble * 100 toLong
    )
  }
  def calcPrice (p: Prices, guestCnt:Int, addGuestsCnt:Int, bkf:Boolean, twin:Boolean, eci:Boolean, lco:Boolean): Double= {

    {
      def pr(cond: Boolean, p: Long) = if (cond) p else 0
      pr(bkf,(addGuestsCnt + guestCnt) * p.get('bkf)) +
      pr(twin, p.get('twin))+
      pr(eci, p.get('eci))+
      pr(lco, p.get('lco))+
      p.get('room)
    }.toDouble / 100

  }

}
