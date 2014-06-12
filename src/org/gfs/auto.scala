package org.gfs

import scala.language.implicitConversions
import java.awt.event.{ActionEvent, ActionListener}
import java.awt.{Container, Component}
import javax.swing._

object auto {

  implicit class AutoClose[A <: AutoCloseable](c:A){
    def autoClose[B](f : A => B ) = {
      try{ f(c)
      }finally { c.close() }
    }
  }
}

object XAction{
  def apply(f : => Unit) = new XAction(f)
}

class XAction(f : => Unit) extends ActionListener{
  override def actionPerformed(e: ActionEvent){ f }
}

object autoGui{

  implicit class autoActionListener[T <: AbstractButton](b: T){
    def $(f : => Unit) = {
      b.addActionListener(XAction(f))
      b
    }
    def event(f : => Unit) = {
      b.addActionListener(XAction(f))
      b
    }
  }

  implicit class autoComponent(p:Container){
    def apply[T <: Component](c: T) = {
      p.add(c)
      c
    }
    def apply[T <: Component](c:T, const:Any) = {
      p.add(c, const)
      c
    }
    def scroll[T <: Component](c: T) = {
      p.add(new JScrollPane(c))
      c
    }
    def scroll[T <: Component](c:T, const:Any) = {
      p.add(new JScrollPane(c), const)
      c
    }
  }

}
