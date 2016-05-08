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

  def tariffs( tariffs: List[Schema.Tariff], from: LocalDateTime, to: LocalDateTime, catId: String): ValidationNel[String, List[Schema.TariffGroup]] = {

    def edgesCheck(tarffs: List[Tariff]) = {
      val (first, last) = (tarffs.head, tarffs.last)
      If(first.get('startDate).compareTo(from) > 0 || last.get('endDate).compareTo(to) < 0)(
         s"""Tariffs of type: ${first.get('name)} are not covering date interval.
             | First tariff start date: ${first.get('startDate)}, last tariff end date: ${last.get('endDate)}.
             | Provided dates: from: $from , to: $to.""".stripMargin
      )
    }

    def gapCheck(t1:Tariff, t2:Tariff)={
      If(t1.get('endDate).compareTo(t2.get('startDate)) < 0)(s"gap between tariffs: 1)$t1, 2)$t2")
    }

    def overlapCheck(t1:Tariff, t2:Tariff)={
      If(t1.get('endDate).compareTo(t2.get('startDate)) > 0)(s"overlap between tariffs: 1)$t1, 2)$t2")
    }

    def startEndCheck(tariffs: List[Tariff]):List[String] = {
      tariffs.flatMap{t=>
        If(t.get('startDate).compareTo(t.get('endDate))>=0)("startDate >= endDate in tariff: :$t")
      }
    }

    //factor out logic that provides checks based on tariff name
    def validated: ValidationNel[String,Map[String, List[Tariff]]] = {

      cutPeriod(from, to, tariffs.sortBy(_.get('startDate)))
      .groupBy(_.get('name)).toList
      .traverseU {
        case (name, rawGroup) =>{

          def checks:List[List[Tariff]=>List[String]]={
            if(name == baseGrpName) List(startEndCheck(_), edgesCheck(_),pairwiseCheck(_)(overlapCheck, gapCheck))
            else List( startEndCheck(_), pairwiseCheck(_)(overlapCheck))
          }
          validate(rawGroup)(checks:_*).map(name -> _)
        }
      }.map(_.toMap).ensure(NonEmptyList("base tariff not found"))(_.contains(baseGrpName))

    }


    validated.map(group(from, to, _))
  }

}
