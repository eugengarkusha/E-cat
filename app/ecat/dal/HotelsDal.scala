package ecat.dal

import java.time.LocalDateTime

import async.client.ObmenSaitPortType
import ecat.model.Schema._
import ecat.model.ops.HotelOps
import ecat.util.DateTime._
import ecat.util.DateTime.{pertrovichDateTimeFormatter => fmt}
import play.api.cache.CacheApi

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.Source

trait HotelsDal {
  def getHotels(from: LocalDateTime, to: LocalDateTime)(implicit ec: ExecutionContext): Future[Seq[Hotel]]
}

class FakeHotelsDal(env: play.api.Environment) extends HotelsDal {
  def getHotels(from: LocalDateTime, to: LocalDateTime)(implicit ec: ExecutionContext): Future[Seq[Hotel]] = {
    val data = Source.fromFile(env.getFile("conf/xml20161013205100_20161027205100"))(scala.io.Codec.UTF8).mkString
    val (from, to) = (LocalDateTime.of(2016, 10, 13, 20, 51, 0), LocalDateTime.of(2016, 10, 27, 20, 51, 0))
    HotelOps.fromXml(scala.xml.XML.loadString(data), from.toLocalDate, to.toLocalDate)
    .fold(err => throw new Exception(err.toString), Future.successful(_))
  }
}

case class CacheSetup(api: CacheApi, ttl: FiniteDuration)

class RealHotelsDal(proxy: ObmenSaitPortType, cacheOpt: Option[CacheSetup]) extends HotelsDal {

  private def fetchData(from: LocalDateTime, to: LocalDateTime) = proxy.getNomSvobod(fmt.format(from), fmt.format(to))

  def getHotels(from: LocalDateTime, to: LocalDateTime)(implicit ec: ExecutionContext): Future[Seq[Hotel]] = {

    def lbl = "H:" + interval(from, to)

    def load: Future[List[Hotel]] = Future(fetchData(from, to)).map { s =>
      //todo: Manage effects!!!
      HotelOps.fromXml(scala.xml.XML.loadString(s), from.toLocalDate, to.toLocalDate).fold(err => throw new Exception(err.toString), identity)
    }

    //do not check on each request!!(inject smth to just do the correct action)
    cacheOpt.fold(load) { cache =>
      cache.api.get[List[Hotel]](lbl).map(Future.successful(_)).getOrElse {
        val hotels = load
        cache.api.set(lbl, hotels, cache.ttl)
        hotels
      }
    }

  }

}
