package ecat.model


import java.time.LocalDate

import ecat.model.Schema._
import ecat.model.ops.ValidationOps._
import schema.heplers.Materializer
import shapeless._
import shapeless.labelled.FieldType
import ecat.util.DateTime.localDateOrdering
import shapeless.ops.record.Selector

import Ordered.orderingToOrdered
import scalaz._
import scalaz.syntax.std.option._
import shapeless.record._


object Validate {
//DO NOT PASS FROM TO EVERYWHERE! validate all structures out of the scope of requested dated and then validate coverage separately!

//)validate all structs as local as possible(children should not know the parents params)
//)optionally revalidate children in parents given the parents data
//)validate price overlaps ,last<=first ,optionsConsistancyCheck on price level;,
  //validate gaps,nonEmpty (based on whether it is base tariff ) , base tariff coverage on tariff level
//) mrege price gaps for tariffs on cat level;

object tariff{

  def apply(t: RawTariff, from: LocalDate, to: LocalDate): \/[String, RawTariff] ={
    if(!t.get('base)) \/-(t)
    else {
      val prices = t.get('prices)

      def gapsChk = {
        pairwiseCheck(prices) { (p, n) =>
          val prevEnd = p.get('endDate)
          val nextStart = n.get('startDate)
          _if(prevEnd < nextStart)(s"base tariff ${t.get('name)}: gap detected in prices between : $prevEnd and $nextStart")
        }
      }

      def pricesCoverReqestIntervalChk = {
        val(first, last) = (prices.head, prices.last)
        def err = {
          s"""base tariff name: ${t.get('name)}, id: ${t.get('id)} has prices not covering date interval.
              | First prices start date: ${first.get('startDate)}, last prices end date: ${last.get('endDate)}.
              | Provided dates: from: $from , to: $to.""".stripMargin
        }
        _if(first.get('startDate).compareTo(from)>0
        || last.get('endDate) < to)(err)
      }


      gapsChk.orElse(pricesCoverReqestIntervalChk).<\/(t)
    }
  }

  def group(group: List[RawTariff]): \/[String, List[RawTariff]] = {

    val baseExists = if(group.forall(_.get('base) == false))Some("no base tariff found") else None
    val noDuplicates: Option[String] = {
      group.map(_.get('name)).groupBy(identity).find(_._2.size > 1).map("duplicate tariffs with name:" + _)
    }

    baseExists.orElse(noDuplicates).<\/(group)
  }
}

  object prices{

    ///thing of implementation using folds and zipWithIndex
    private object chkOptConsistancy extends Poly1 {
      def chk(w: Witness, group :List[Prices])(implicit selector : Selector.Aux[Prices, w.T, Option[Long]]): Option[String] = {
        if(group.size == 1) None
        else if(group.map(p => p.get(w).isDefined).reduce(_ == _)) None
        else Some(s"prices options consistancy check failed for: ${w.value}")
      }
      implicit def c[K, V](implicit w: Witness.Aux[K],  selector : Selector.Aux[Prices, K, Option[Long]]) = {
        at[(FieldType[K, V], List[Prices])]{case(_, grp) => chk(w, grp)}
      }
    }


    def group(pricesGrp: List[Prices]): \/[String, List[Prices]] = {


      def optionsConsistancy: Option[String] = {
          implicitly[Materializer[Prices]].v.take(5)
          .zipConst(pricesGrp)
          .map(chkOptConsistancy).toList
          .reduce(_ orElse _)
      }

      def overlap: Option[String]  = pairwiseCheck(pricesGrp){ (p, n) =>
        val prevEnd = p.get('endDate)
        val nextStart = n.get('startDate)
        _if(prevEnd > nextStart)(s"overlap detected between : $prevEnd and $nextStart")
      }

      def empty: Option[String] = {
        _if(pricesGrp.isEmpty)("prices cannot be empty")
      }

      (empty orElse overlap orElse optionsConsistancy) <\/ pricesGrp

    }

    def apply (p: Prices): \/[String, Prices] = {
        if(p.get('startDate) >= p.get('endDate))-\/("startDate >= endDate")
        else \/-(p)
    }
  }



}
