package ecat.model.ops

import ecat.model.Schema._
import shapeless.record._

object PricesOps {

  def calcPrice (p: Prices, bkf:Boolean, eci:Boolean, lco:Boolean): Double= {

    def addIf(cond: Boolean, l: Long, r:Long) = if (cond) l + r else r

    (addIf(lco, p.get('lco),
        addIf(eci, p.get('eci),
          addIf(bkf, p.get('bkf), p.get('room))))
    ).toDouble / 100
  }

}
