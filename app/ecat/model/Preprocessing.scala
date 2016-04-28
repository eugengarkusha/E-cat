package ecat.model
import java.time.LocalDateTime

import Schema._
import shapeless._, record._
import scalaz.{Ordering => _, _}
import Scalaz._
import ecat.util.DateTime._


object Preprocessing {

  def rooms(r: List[Schema.Room]):ValidationNel[String, List[Schema.Room]] = {
//    if(r.get('options).isEmpty)"room options may not be empty".failureNel else Success(r)
      Success(r)
  }

  //failing fast here
  def tariffs( tariffs: List[Schema.Tariff], from: LocalDateTime, to: LocalDateTime): ValidationNel[String, List[Schema.Tariff]] = {

    val filtered: List[Schema.Tariff] = tariffs.sortBy(_.get('startDate))
      .dropWhile(_.get('endDate).compareTo(from) < 0)
      .takeWhile(_.get('startDate).compareTo(to) <= 0)

    {
      if (filtered.isEmpty) s"no active tariffs in category $this".failureNel[List[Schema.Tariff]]
      else if (filtered.head.get('startDate).compareTo(from) > 0 || filtered.last.get('endDate).compareTo(to) < 0) {
        s"""tariffs are not covering date interval: first tariff start date :${tariffs.head.get('startDate)},
            |last tariff end date:${tariffs.last.get('endDate)}.Provided dates: $from:, $to.
            |""".stripMargin.failureNel
      }
      else {
        filtered.sliding(2, 1).find(t => t.size==2 && t(0).get('endDate) != t(1).get('startDate))
        .map(s => s"gap or overlap between tariffs: 1)${s(0)}, 2)${s(1)}")
        .toFailureNel{
          //aligning tariffs  start/end dates with given (category) interval
          val t = (filtered.head.updated('startDate, from) +: filtered.tail)
          t.init :+ t.last.updated('endDate, to)
        }
      }
    }
  }

}
