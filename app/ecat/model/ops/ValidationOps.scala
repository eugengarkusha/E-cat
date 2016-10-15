package ecat.model.ops

import scala.util.Try
import org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace
import scalaz.Scalaz._
import scalaz._

object ValidationOps {

  def validate[T](data: T)(checks: (T => List[String])*): ValidationNel[String, T] = {
    val errs = checks.flatMap(_(data))
    if (errs.isEmpty) data.successNel
    else NonEmptyList(errs.head, errs.tail: _*).failure
  }

  def pairwiseCheck[T, H[X] <: Seq[X]](data: H[T])(checks: ((T, T) => Option[String])*): Option[String] = {
    val i = data.sliding(2, 1).filter(_.size == 2).flatMap(l => checks.flatMap(_ (l(0), l(1))))
    //Todo: this is stupid(pimp it with headopton or smth)
    if(i.hasNext)Some(i.next()) else None
  }

  def _if(cond: Boolean)(err: String): Option[String] = if (cond) Some(err) else None

  def _catch[T](lbl: String)(v: => T): \/[String, T] = {

    //todo: Truncate stacktrace str(ans check that it works aas expected!!)
    Try(v).toDisjunction.leftMap(err=> lbl+ ", stacktrace:\n" + getStackTrace(err))
  }

}
