package ecat

import javax.xml.ws.BindingProvider

import play.api._
import ApplicationLoader.Context
import router.Routes
import _root_.controllers.{Assets, Application => AppController}
import async.client.ObmenSait
import async.client.ObmenSaitPortType
import _root_.controllers.BlockApi
import _root_.controllers.Debug
import ecat.wiring.BlockDalModule
import ecat.wiring.HotelsDalModule
import ecat.wiring.ProxyModule
import ecat.wiring.WithConfig
import ecat.wiring.WithEnvironment
import play.api.cache.CacheApi
import play.api.cache.EhCacheComponents


class AppLoader extends ApplicationLoader {
  override def load(context: Context): Application = new Components(context).application
}

class Components(context: Context)
 extends BuiltInComponentsFromContext(context)
 with EhCacheComponents
 with HotelsDalModule
 with BlockDalModule
 with ProxyModule
 with WithConfig
 with WithEnvironment
 {

  val config =  configuration.underlying
  implicit val ec = scala.concurrent.ExecutionContext.global


  lazy val router = {
    new Routes(
      httpErrorHandler,
      new AppController(hotelsDal),
      new Assets(httpErrorHandler),
      new Debug(proxy),
      new BlockApi(blockDal)
    )
  }
}