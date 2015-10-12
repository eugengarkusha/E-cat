package controllers

import java.time.LocalDate

import play.api._
import play.api.mvc._

class Application extends Controller {

 //TODO: delete
//  def index = Action {
//    Ok(views.html.index("Your new application is ready."))
//  }


  //TODO: thing of correct name for this
  //return the rendered booking template here
  def bookingCategories(from:LocalDate,to:LocalDate) =Action {
    Ok(s"Success from =$from, to=$to")
  }

}
