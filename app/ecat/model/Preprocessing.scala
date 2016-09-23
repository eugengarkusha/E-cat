package ecat.model
import java.time.LocalDateTime
import java.time.temporal._

import Schema._
import ecat.model.ops.TariffOps._
import ecat.model.ops.ValidationOps._
import shapeless._
import record._
import scalaz.{Ordering => _, _}
import scalaz._
import Scalaz._
import ecat.util.DateTime._


object Preprocessing {

  def rooms(r: List[Schema.Room]):ValidationNel[String, List[Schema.Room]] = {
//    if(r.get('options).isEmpty)"room options may not be empty".failureNel else Success(r)
      Success(r)
  }

  //TODO: Add validation of negative values consistsncy in tariff group
  def tariffs( tariffs: List[Schema.Tariff], from: LocalDateTime, to: LocalDateTime, catId: String): ValidationNel[String, List[Schema.TariffGroup]] = {

    def nonEmptyCheck(group: List[Tariff]): List[String] = _if(group.isEmpty)("no tariffs provided")

    def edgesCheck(group: List[Tariff]): List[String] = {
      val (first, last) = (group.head, group.last)
      def err = {
        s"""Tariffs of type: ${first.get('name)}, id: ${first.get('id)} are not covering date interval.
            | First tariff start date: ${first.get('startDate)}, last tariff end date: ${last.get('endDate)}.
            | Provided dates: from: $from , to: $to.""".stripMargin
      }
      _if(first.get('startDate).compareTo(from) > 0 || last.get('endDate).compareTo(to) < 0)(err)
    }

    def gapCheck(t1: Tariff, t2: Tariff): List[String] = {
      _if(t1.get('endDate).compareTo(t2.get('startDate)) < 0)(s"gap between tariffs: 1)$t1, 2)$t2")
    }

    def overlapCheck(t1: Tariff, t2: Tariff): List[String] = {
      _if(t1.get('endDate).compareTo(t2.get('startDate)) > 0)(s"overlap between tariffs: 1)$t1, 2)$t2")
    }

    def startEndCheck(group: List[Tariff]): List[String] = {
      group.flatMap{t=>
        _if(t.get('startDate).compareTo(t.get('endDate)) >= 0)(s"startDate >= endDate in tariff: :$t")
      }
    }

    def optionsConsistancyCheck(group: List[Tariff]): List[String] = {

      val t@(name, sz) = group.head.get('name) -> group.size
      val ppd = group.map(_.get('pricesPerDay))
      val bkfCnt = ppd.count(_.get('bkf).isDefined)
      val twinCnt = ppd.count(_.get('twin).isDefined)

      def chk(cnt: Int, lbl: String): List[String] = {
        _if(cnt > 0  && cnt < sz)(s"tariffGroup '$name': $lbl availability is inconsistent")
      }

      chk(bkfCnt, "breakfast") ++ chk(twinCnt, "twin")
    }

    //factor out logic that provides checks based on tariff name
    def validated: ValidationNel[String,Map[String, List[Tariff]]] = {

      cutPeriod(from, to, tariffs.sortBy(_.get('startDate)))
      .groupBy(_.get('name)).toList
      .traverseU {
        case (name: String, rawGroup: List[Tariff]) =>{

          def checks: List[List[Tariff] => List[String]] = {
            if(name == baseGrpName) List(nonEmptyCheck _, startEndCheck _ , edgesCheck(_), pairwiseCheck(_)(overlapCheck, gapCheck), optionsConsistancyCheck _)
            else List(nonEmptyCheck _, startEndCheck(_), pairwiseCheck(_)(overlapCheck), optionsConsistancyCheck _)
          }
          validate(rawGroup)(checks:_*).map(name -> _)
        }
      }.map(_.toMap).ensure(NonEmptyList("base tariff not found"))(_.contains(baseGrpName))

    }


    validated.map(group(from, to, _))
  }

}
