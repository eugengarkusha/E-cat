package ecat.model

import ecat.util.DateTimeFormatters.{pertrovichDateTimeFormatter => fmt}
import java.time.{LocalDateTime}

import play.api.mvc.PathBindable.Parsing

/**
 * Created by admin on 10/12/15.
 */
object Bindables {

  implicit val ldf = new Parsing[LocalDateTime](LocalDateTime.parse(_, fmt), fmt.format(_), _ +":"+ _.getMessage)

}