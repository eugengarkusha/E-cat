package ecat.model

import ecat.util.JsonFormats._

import scalaz.{Lens => _,Category=>_, _}
import Scalaz._
import ecat.model.Schema._
import play.api.libs.json.Reads
import schema.RecordFilters._
import schema.RecordJsonFormats._

object Filters {
  implicit val hotelFilterReads = implicitly[Reads[Filter[Hotel]]]
  implicit val categoryFilterReads = implicitly[Reads[Filter[Category]]]
}


