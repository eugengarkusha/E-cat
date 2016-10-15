package ecat.model.ops

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import ecat.util.DateTime.{pertrovichDateTimeFormatter => fmt}
import shapeless.contrib.scalaz.instances._
import shapeless.record._
import shapeless.poly.->
import shapeless.syntax.singleton._

import ecat.model.Schema._
import ValidationOps._catch
import shapeless.Poly1
import shapeless.record._
import ecat.util.FieldTypeInstances._
import ecat.model.Validate
import ecat.model.Validate.prices
import scalaz._
import Scalaz._
import scala.xml.Node


object PricesOps {

  def fromXml(n: Node): String \/ Prices = {


    def price(a: String) = {
      val p = (a.toDouble * 100).toLong
      if(p >= 0) Some(p) else None
    }

    def date(d: String) = LocalDateTime.parse(d, fmt).toLocalDate

    _catch("exception while parsing Prices payload"){
      Record(
        room = price(n \@ "roomprice"),
        bkf  = price(n \@ "bkfprice"),
        twin = price(n \@ "twinprice"),
        eci  = price(n \@ "eciprice"),
        lco  = price(n \@ "lcoprice"),
        startDate = date(n \@ "dateN"),
        endDate = date(n \@ "dateK")
      )
    }.flatMap(Validate.prices(_))

  }


  //returns overal base price along with overal calculator
  def overallCalculator(basePrices: List[Prices], from: LocalDate, to: LocalDate): (Prices, (List[Prices] => Prices)) = {

    def sampleDates(from: LocalDate, to: LocalDate): Stream[LocalDate] = {
      lazy val k: Stream[LocalDate]= (from #:: k.map(_.plusDays(1)))
      k.takeWhile(_ != to)
    }

    val inRange: Set[LocalDate] = sampleDates(from, to).toSet

    def samplePrices(prices: List[Prices]): Map[LocalDate, Prices] = {
       prices.flatMap(p => sampleDates(p.get('startDate), p.get('endDate)).withFilter(inRange).map(_ -> p))(collection.breakOut)
    }

    def overall (sampledPrices: Iterable[Prices]): Prices = {
      sampledPrices.map(_.take(3)).reduce(_ |+| _) +
      ('eci ->> sampledPrices.head.get('eci)) +
      ('lco ->> sampledPrices.last.get('lco)) +
      ('startDate ->> from)+
      ('endDate ->> to)
    }

    val baseSampled = samplePrices(basePrices)

    overall(baseSampled.values) -> (prices => overall(baseSampled.unionWith(samplePrices(prices))((_, b) => b).values))
  }


  def calcPrice(p: Prices, guestCnt: Int, addGuestsCnt: Int, bkf: Boolean, twin: Boolean, eci: Boolean, lco: Boolean): Double = {

    def pr(enabled: Boolean, price: Option[Long]): Long = {
      if(!enabled) 0
      else price.getOrElse{
        println("WARNING: trying to enable unavailable price options")
        0
      }
    }

    {
      pr(bkf, p.get('bkf).map(_ * (addGuestsCnt + guestCnt))) +
      pr(twin, p.get('twin)) +
      pr(eci, p.get('eci)) +
      pr(lco, p.get('lco)) +
      p.get('room).getOrElse(0L)
    }.toDouble / 100

  }

}
