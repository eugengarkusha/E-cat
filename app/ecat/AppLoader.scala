package ecat

import javax.xml.ws.BindingProvider

import play.api._
import ApplicationLoader.Context
import router.Routes
import _root_.controllers.{Assets, Application => AppController}
import async.client.ObmenSait
import async.client.ObmenSaitPortType
import controllers.BlockApi

import play.api.cache.EhCacheComponents


class AppLoader extends ApplicationLoader {
  override def load(context: Context): Application = new Components(context).application
}

class Components(context: Context) extends BuiltInComponentsFromContext(context) with EhCacheComponents{

  val proxy: ObmenSaitPortType ={
    val srv = new ObmenSait().getObmenSaitSoap()
    val req_ctx =  srv.asInstanceOf[BindingProvider].getRequestContext
    req_ctx.put(BindingProvider.USERNAME_PROPERTY, "sait");
    req_ctx.put(BindingProvider.PASSWORD_PROPERTY, "sait555");
    srv
  }

  lazy val router = {
    new Routes(
      httpErrorHandler,
      new AppController(defaultCacheApi, context.environment, proxy),
      new Assets(httpErrorHandler),
      new BlockApi(proxy)
    )
  }
}