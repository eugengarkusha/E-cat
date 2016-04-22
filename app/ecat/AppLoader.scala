package ecat

import play.api._,ApplicationLoader.Context
import router.Routes
import _root_.controllers.{Assets, Application => AppController}
import play.api.cache.EhCacheComponents


class AppLoader extends ApplicationLoader {
  override def load(context: Context): Application = new Components(context).application
}

class Components(context: Context) extends BuiltInComponentsFromContext(context) with EhCacheComponents{
  lazy val router = new Routes(httpErrorHandler, new AppController(defaultCacheApi, context.environment), new Assets(httpErrorHandler) )
}