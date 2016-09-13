package ecat.util

import shapeless.HList
import shapeless.contrib.scalaz.MonoidDerivedOrphans
import shapeless.ops.record.Values
import shapeless.ops.traversable.FromTraversable

import scala.collection.GenTraversable
import scalaz.Monoid

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

}
