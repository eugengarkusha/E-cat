package ecat.model

import ecat.util.JsonFormats._

import scalaz.{Lens => _, _}
import Scalaz._
import ecat.model.Schema._
import play.api.libs.json.Reads
import schema.RecordFilters._
import schema.RecordJsonFormats._
import schema.heplers.Materializer._
import schema.heplers.misc.tpe

object Filters {
  implicit val filterReads = implicitly[Reads[Filter[Hotel]]]
}


