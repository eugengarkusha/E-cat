package ecat.model

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import ecat.model.Schema.{Prices, Tariff, TariffGroup}

import scala.annotation.tailrec
import shapeless._
import record._
import syntax.std.traversable._
import ecat.util.RecordInstances._
import scalaz._
import Scalaz._
import shapeless.contrib.scalaz.instances._

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

      def getFromBase(start: LocalDateTime,days:Int):List[Tariff]={
        val end = start.plus(days, ChronoUnit.DAYS)
        alignToDates(start, end, cutPeriod(start, end, baseGrp))
      }

      @tailrec
      def m (start :LocalDateTime, grp:List[Tariff], merged:List[Tariff]):List[Tariff] ={
        if(grp.isEmpty)merged
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
        (tariff.get('pricesPerDay)).toList.map(_ * days).toHList[Schema.Prices].get
      }.reduce(_ |+| _)
    }



    val baseGroup: List[Tariff] = alignToDates(from ,to,rawGroups(baseGrpName))

    Record(name = baseGrpName, tariffs = baseGroup, overalPrices = overalPrices(baseGroup))::
    (rawGroups - baseGrpName).map{ case (name ,rawGroup) =>
        val mergedGroup = mergeAligned(baseGroup, alignToDates(from, to,rawGroup))
        Record(name = name, tariffs = mergedGroup, overalPrices = overalPrices(mergedGroup))
    }.toList



  }


  //tariffs should come sorted by startDate!
  def cutPeriod(start:LocalDateTime, end:LocalDateTime, tariffs:List[Tariff])={
    tariffs.dropWhile(_.get('endDate).compareTo(start) <= 0).takeWhile(_.get('startDate).compareTo(end) < 0)
  }
}
