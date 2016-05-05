package ecat.model

import scalaz._, Scalaz._

object ValidationOps {

  def validate[T](data:T)(checks:(T=>List[String])*): ValidationNel[String, T]={
    val errs = checks.flatMap(_(data))
    if(errs.isEmpty)data.successNel
    else NonEmptyList(errs.head, errs.tail:_*).failure
  }

  def pairwiseCheck[T,H[X]<:Seq[X]](data: H[T])(checks:((T, T)=>Seq[String])*): List[String] ={
    data.sliding(2,1).filter(_.size ==2).flatMap(l=>checks.flatMap(_(l(0),l(1)))).toList
  }

  def If(cond: Boolean)(err: String)= if(cond)List(err)else Nil

}
