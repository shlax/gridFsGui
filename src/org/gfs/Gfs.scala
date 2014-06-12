package org.gfs

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext

object Gfs {

  val pull = Executors.newCachedThreadPool()

  object implicits{
    implicit val ec = ExecutionContext.fromExecutor(pull)
  }

}
