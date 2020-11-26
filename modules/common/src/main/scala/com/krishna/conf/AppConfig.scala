package com.krishna.conf

import com.typesafe.config.{ Config, ConfigFactory }

trait AppConfig {

  // TODO inject this as a dependency

  def config: Config = ConfigFactory.load("application")

}
