package ecat.model.ops

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import ecat.util.DateTime.{pertrovichDateTimeFormatter => fmt}
import ecat.model.Schema.{Prices, Tariff, TariffGroup}
import ecat.util.DateTime._
import shapeless._
import shapeless.contrib.scalaz.instances._
import shapeless.record._
import ecat.util.RecordInstances._
import ecat.model.ops.PricesOps._
import scala.annotation.tailrec
import scalaz.Scalaz._
import scalaz._
import shapeless.syntax.std.traversable._

import scala.xml.Node

object TariffOps {
  val baseGrpName = "ПРОЖИВАНИЕ"

  def group(from: LocalDateTime, to: LocalDateTime, rawGroups: Map[String, List[Tariff]]):List[TariffGroup] ={

    def alignToDates (start: LocalDateTime, end:LocalDateTime, tariffs: List[Tariff]):List[Tariff] = {
      val withHeadUpdated = {
        if(tariffs.head.get('startDate).compareTo(start)<0) tariffs.head.updated('startDate, start) :: tariffs.tail
        else tariffs
      }

      withHeadUpdated.init :+ {
        if(withHeadUpdated.last.get('endDate).compareTo(end)>0) withHeadUpdated.last.updated('endDate, end)
        else withHeadUpdated.last
      }
    }
    //groups should come aligned to date boundaries here
    def mergeAligned(baseGrp: List[Tariff], otherGrp: List[Tariff]): List[Tariff] ={

      def getFromBase(start: LocalDateTime, days:Int):List[Tariff]={
        assert(days >= 0)
        if(days == 0)Nil
        else {
          val end = start.plus(days, ChronoUnit.DAYS)
          alignToDates(start, end, cutPeriod(start, end, baseGrp))
        }
      }

      @tailrec
      def m (start :LocalDateTime, grp:List[Tariff], merged:List[Tariff]):List[Tariff] ={
        if(grp.isEmpty) merged.++(getFromBase(start, ChronoUnit.DAYS.between(start, to).toInt))
        else {
          val tariffs = {
            val gap = ChronoUnit.DAYS.between(start, grp.head.get('startDate)).toInt
            if (gap == 0) Seq(grp.head)
            else getFromBase(start, gap) :+ grp.head
          }
          m(grp.head.get('endDate), grp.tail, merged ++ tariffs)
        }
      }
      m(baseGrp.head.get('startDate), otherGrp, List.empty[Tariff])
    }

    def overalPrices(group: List[Tariff]):Prices = {
      group.map {tariff =>
        val days = ChronoUnit.DAYS.between(tariff.get('startDate), tariff.get('endDate))
        (tariff.get('pricesPerDay)).toList.map(_ * days).toHList[Prices].get
      }.reduce(_ |+| _)
    }



    val baseGroup: List[Tariff] = alignToDates(from ,to,rawGroups(baseGrpName))

    Record(name = baseGrpName, tariffs = baseGroup, overalPrices = overalPrices(baseGroup))::
    (rawGroups - baseGrpName).map{ case (name ,rawGroup) =>
        val mergedGroup = mergeAligned(baseGroup, alignToDates(from, to, rawGroup))
        Record(name = name, tariffs = mergedGroup, overalPrices = overalPrices(mergedGroup))
    }.toList

  }

  //tariffs should come sorted by startDate!
  def cutPeriod(start:LocalDateTime, end:LocalDateTime, tariffs:List[Tariff])={
    tariffs.dropWhile(_.get('endDate).compareTo(start) <= 0).takeWhile(_.get('startDate).compareTo(end) < 0)
  }


  def fromXml(n: Node):Tariff = Record(
    id = n \@"id" ,
    name = n \@"name" ,
    startDate = LocalDateTime.parse(n \@"dateN", fmt) ,
    endDate = LocalDateTime.parse(n \@"dateK", fmt) ,
    pricesPerDay = Record(
      room = (n \@"roomprice").toDouble * 100 toLong,
      bkf = (n \@"bkfprice").toDouble * 100 toLong,
      eci = (n \@"eciprice").toDouble * 100 toLong,
      lco = (n \@"lcoprice").toDouble * 100 toLong
    )
  )
}
