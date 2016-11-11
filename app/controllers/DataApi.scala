package controllers

import java.time.LocalDateTime

import ecat.dal.HotelsDal
import ecat.model.Schema.Hotel
import play.api.mvc.Action
import play.api.mvc.BodyParsers
import play.api.mvc.Controller
import schema.RecordFilters.Filter

//For future for single page app development
class DataApi(dal: HotelsDal) extends Controller {
//  def getHotels(from: LocalDateTime, to: LocalDateTime) = Action.async(BodyParsers.parse.json[Filter[Hotel]]) { req =>
//    import req.body
//  }

}
