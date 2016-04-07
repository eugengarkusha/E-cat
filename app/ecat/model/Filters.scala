package ecat.model

import play.api.libs.json.{JsValue, Reads}
import shapeless._,record._,labelled._
import scalaz.\/


object Filters {


  type Filter[V, T] = (V, Seq[T]) => Seq[T]
  type ParsedFilter[V, T] = (V, Seq[T]) => String\/Seq[T]

   def filterByNonEmpty[T,V[X]<:Seq[X],X](l:Lens[T,V[X]])(s:Seq[T])(tf:V[X]=>V[X]):Seq[T]={
      s.iterator.map(t=> l.set(t)(tf(l.get(t)))).filter(l.get(_).nonEmpty).toSeq
   }

  val pureFilters = Record(
    twin = (isTwin:Boolean, hotels:Seq[Hotel])=> {
      filterByNonEmpty(lens[Hotel] >> 'categories)(hotels){ cats =>
        filterByNonEmpty(lens[Category] >> 'numbers)(cats)(_.filter(_.twin == isTwin))
      }
    }
  )

  object toParsedFilter extends Poly1{
    implicit def caseT[K<:Symbol,V,T](implicit r :Reads[V], w:Witness.Aux[K]) = at[FieldType[K,Filter[V,T]]](f=>
      w.value.name -> ((jv:JsValue, s:Seq[T])=> \/.fromEither(r.reads(jv).map(f(_, s)).asEither).leftMap(_.toString))
    )
  }

  val jvFilters:Map[String, ParsedFilter[JsValue, Hotel]] = pureFilters.map(toParsedFilter).toList.toMap
}
