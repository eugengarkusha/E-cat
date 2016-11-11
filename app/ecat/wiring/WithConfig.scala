package ecat.wiring

import com.typesafe.config.Config

trait WithConfig {
  def config : Config
}
