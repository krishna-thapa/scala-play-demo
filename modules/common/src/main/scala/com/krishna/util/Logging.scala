package com.krishna.util

import org.slf4j.{ Logger, LoggerFactory }

/**
  * Trait to add Logging to a Scala class
  */
trait Logging {

  protected lazy val log: Logger = LoggerFactory.getLogger(this.getClass)
}
