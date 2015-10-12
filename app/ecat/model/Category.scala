package ecat.model
import java.time.LocalDate
import java.util.Date

/**
 * Created by admin on 10/12/15.
 */

//properties = additional details about number setup. They may become the dynamic part of filters .
case class Room(guestsCnt: Int, additionalGuestsCnt: Int, twin:Boolean, properties:Map[String,String])
case class Tariff(id: String,startDate: LocalDate, endDate: LocalDate, price:Double, breakfastPrice: Double, earlyСheckInPrice: Double, lateCHeckOutPrice: Double)
case class Category (name: String, tariffs: Seq[Tariff], numbers: Seq[Number])

