package org.gfs.mongo

import scala.language.implicitConversions

object auto {

  implicit class AutoClose[A <: AutoCloseable](c:A){
    def autoClose[B](f : A => B ) = {
      try{ f(c)
      }finally { c.close() }
    }
  }
}

