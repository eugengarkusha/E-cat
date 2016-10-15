package ecat.model

import java.time.temporal.ChronoUnit
import java.time.{LocalDateTime, LocalTime, LocalDate}
import schema.heplers.misc.{tpe, TypeCaptured}
import shapeless._
import record._
import shapeless.labelled._
import shapeless.syntax.singleton._


/**
 * Created by admin on 10/12/15.
 */

object Schema {
  //aliases for value level schema encoding
  type RawTariff = rawTariff.tpe
  type Tariff = tariff.tpe

  type Room = Record.`'number -> Int, 'guestsCnt -> Int, 'additionalGuestsCnt -> Int, 'twin -> Boolean, 'bathroom -> String, 'level -> Int, 'options -> List[String]`.T
  type Prices = Record.`'room -> Option[Long], 'bkf -> Option[Long], 'twin -> Option[Long], 'eci -> Option[Long], 'lco -> Option[Long], 'startDate -> LocalDate, 'endDate -> LocalDate`.T
  //value level schema encoding(here used for adding fields to the end of the record which otherwise seems not possible)
  val rawTariff = TypeCaptured(Record(id = tpe[String], base = tpe[Boolean], name = tpe[String], description = tpe[String], prices = tpe[List[Prices]]))
  val tariff = TypeCaptured(rawTariff.value + ('overallPrices ->> tpe[Prices]))
  type Category = Record.`'id -> String, 'name -> String, 'rooms -> List[Room], 'tariffs -> List[Tariff]`.T
  type Hotel =  Record.`'id -> String, 'name -> String, 'checkInTime -> LocalTime, 'checkOutTime -> LocalTime,'eci -> LocalTime, 'lco -> LocalTime, 'categories -> List[Category]`.T


}
