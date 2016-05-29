package ecat.model

import java.time.temporal.ChronoUnit
import java.time.{LocalDateTime, LocalTime}
import shapeless._, record._

/**
 * Created by admin on 10/12/15.
 */

object Schema {
  type Room = Record.`'number->Int, 'guestsCnt->Int, 'additionalGuestsCnt-> Int, 'twin->Boolean, 'bathroom->String, 'level-> Int, 'options-> List[String]`.T
  type Prices = Record.`'room-> Long, 'bkf-> Long,'twin->Long, 'eci-> Long, 'lco-> Long`.T
  type Tariff = Record.`'id-> String, 'name->String, 'startDate-> LocalDateTime, 'endDate-> LocalDateTime, 'pricesPerDay -> Prices`.T  //'roomPrice-> Long, 'bkf-> Long, 'eci-> Long, 'lco-> Long`
  type TariffGroup = Record.`'name->String, 'tariffs->List[Tariff], 'overalPrices->Prices`.T
  type Category = Record.`'id-> String, 'name->String, 'rooms-> List[Room], 'tariffGroups-> List[TariffGroup]`.T
  type Hotel =  Record.`'id-> String, 'name-> String, 'checkInTime-> LocalTime, 'checkOutTime-> LocalTime,'eci-> LocalTime, 'lco-> LocalTime, 'categories-> List[Category]`.T
}
