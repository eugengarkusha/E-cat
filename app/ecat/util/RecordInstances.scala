package ecat.util

import shapeless.HList
import shapeless.Witness
import shapeless.contrib.scalaz.{MonoidDerivedOrphans, ShowDerivedOrphans}
import shapeless.labelled._
import shapeless.ops.record.Values
import shapeless.ops.traversable.FromTraversable

import scala.collection.GenTraversable
import scalaz.Monoid
import scalaz.Show

object RecordInstances {

  //needs import shapeless.contrib.scalaz.instances._ to be imported
  implicit def recordMonoid[R <: HList, H <: HList](implicit v: Values.Aux[R, H], p: Monoid[H]): Monoid[R] = {
    MonoidDerivedOrphans.typeClass.project(p, (r: R) => v.apply(r), (h: H) => h.asInstanceOf[R])
  }


  implicit def fromTraversable[R <: HList, H <: HList](implicit v: Values.Aux[R, H], p: FromTraversable[H]): FromTraversable[R] = {
    new FromTraversable[R] {
      def apply(l: GenTraversable[_]): Option[R] = p.apply(l).asInstanceOf[Option[R]]
    }
  }
  //to be used with HList derived show instance provided by shapeless contrib (think of implementing derivation like in shapeless.examples(and contributingit to contrib?))
  //think of combining this implicit with Hlist TC derivations(Recors Show instance based on Hlist and FT  Show derivations)
  implicit def FTShow[K,V](implicit w: Witness.Aux[K], s: Show[V]): Show[FieldType[K, V]] = new Show[FieldType[K, V]] {
    override def shows(f: FieldType[K, V]): String = s"${w.value}:${s.shows(f)}"
  }

}
