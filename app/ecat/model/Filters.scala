package ecat.model

import ecat.util.JsonFormats._
import scalaz.{Lens => _, _}
import Scalaz._
import ecat.model.Schema._
import schema.RecordFilters
import schema.RecordJsonFormats._
import schema.heplers.misc.tpe

object Filters {
  val filters = RecordFilters.from(tpe[Hotel])
}


