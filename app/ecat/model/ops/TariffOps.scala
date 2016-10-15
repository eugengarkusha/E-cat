package ecat.model.ops

import java.time.LocalDate

import ecat.model.Schema.RawTariff
import ecat.model.Schema.Tariff
import ecat.model.Validate
import ecat.util.DateTime.{pertrovichDateTimeFormatter => fmt}
import shapeless.contrib.scalaz.instances._
import shapeless.record._
import shapeless.syntax.singleton._
import ValidationOps._catch
import scalaz.Scalaz._
import ecat.util.FieldTypeInstances._
import ecat.model.ops.PricesOps.overallCalculator
import shapeless.Poly1
import shapeless.record.Record
import scalaz.syntax.std._
import scala.xml.Node
import scalaz.\/
import scalaz.syntax.bind.ToBindOps

object TariffOps {
  val baseGrpName = "ПРОЖИВАНИЕ"

  def isTwinAvailable(t: Tariff):Boolean = t.get('overallPrices).get('twin).isDefined
  def isBkfAvailable(t: Tariff): Boolean = t.get('overallPrices).get('bkf).isDefined

  def addOverallPrices(tariffs: List[RawTariff], from: LocalDate, to: LocalDate): List[Tariff] = {
    val (List(base), others) = tariffs.partition(_.get('base))
    val (baseOverallPrices, calc) = overallCalculator(base.get('prices), from, to)

    base.+('overallPrices ->> baseOverallPrices ) ::
    others.map(t => t + ('overallPrices ->> calc(t.get('prices))))
  }



  //catch in outer scope!!
  def rawFromXml(n: Node): \/[String, RawTariff] = {
    _catch("exception while parsing Tariff payload") {
      val baseTarifName = "стандартный тариф"
      val id = n \@ "id"
      val name = n \@ "name"
      val pricesNodes = (n \ "price").toList
      val description = n \@ "description"
      pricesNodes.traverseU(PricesOps.fromXml(_))
      .flatMap(Validate.prices.group(_))
      .bimap(
        err => s"tariff id:$id, name $name: $err",
        prices => Record(id = id, base = (name == baseTarifName), name = name, description = description, prices = prices)
      )
    }.join
  }
}
