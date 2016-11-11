package ecat.wiring
import ecat.dal._
import play.api.cache.EhCacheComponents
import scala.concurrent.duration._

trait HotelsDalModule { self: WithConfig with WithEnvironment with ProxyModule with EhCacheComponents =>
  lazy val hotelsDal: HotelsDal = {
    if(config.getBoolean("fakedata"))new FakeHotelsDal(environment)
    else new RealHotelsDal(proxy, Some(CacheSetup(defaultCacheApi, config.getInt("cache.ttl").minutes)))
  }
}
