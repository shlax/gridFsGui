package org.gfs

import scala.language.implicitConversions
import java.awt.event.{ActionEvent, ActionListener}

object auto {

  implicit class AutoClose[A <: AutoCloseable](c:A){
    def autoClose[B](f : A => B ) = {
      try{ f(c)
      }finally { c.close() }
    }
  }
}

object autoGui{

  implicit def $[T](f: => T) = new ActionListener(){
    override def actionPerformed(e: ActionEvent){ f }
  }

}
