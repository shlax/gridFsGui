package org.gfs

import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors

object Gfs {

  val pull = Executors.newCachedThreadPool()

  object implicits{
    implicit val ec = ExecutionContext.fromExecutor(pull)
  }

}
