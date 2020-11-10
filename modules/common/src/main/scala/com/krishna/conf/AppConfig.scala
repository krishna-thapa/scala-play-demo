package com.krishna.conf

import com.typesafe.config.{ Config, ConfigFactory }

trait AppConfig {

  def config: Config = ConfigFactory.load("application")

}
