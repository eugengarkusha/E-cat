package ecat.model

import play.api.libs.json.{JsValue, Reads}
import shapeless._,record._,labelled._
import scalaz.\/

object Filters {


  type Filter[V, T] = (V, Seq[T]) => Seq[T]
  type ParsedFilter[V, T] = (V, Seq[T]) => String\/Seq[T]


  //TODO: rewrite with Monocle(or shapeless lenses)
  val pureFilters = Record(
    twin = (isTwin:Boolean, hotels:Seq[Hotel])=> {
      hotels.iterator.map{h=>
        val c = h.categories.iterator.map(c=> c.copy(numbers = c.numbers.filter(_.twin == isTwin))).filterNot(_.numbers.isEmpty)
        h.copy(categories = c.toSeq)
      }.filterNot(_.categories.isEmpty).toSeq
    }
  )


  object toParsedFilter extends Poly1{
    implicit def caseT[K<:Symbol,V,T](implicit r :Reads[V], w:Witness.Aux[K]) = at[FieldType[K,Filter[V,T]]](f=>
      w.value.name -> ((jv:JsValue, s:Seq[T])=> \/.fromEither(r.reads(jv).map(f(_, s)).asEither).leftMap(_.toString))
    )
  }

  val jvFilters:Map[String, ParsedFilter[JsValue, Hotel]] = pureFilters.map(toParsedFilter).toList.toMap


}
