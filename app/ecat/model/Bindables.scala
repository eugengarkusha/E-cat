package ecat.model

import Formatters.{dateFormatter =>fmt}
import java.time.LocalDate
import play.api.mvc.PathBindable.Parsing

/**
 * Created by admin on 10/12/15.
 */
object Bindables {

  implicit val ldf = new Parsing[LocalDate](LocalDate.parse(_, fmt), fmt.format(_), _ +":"+ _.getMessage)

}