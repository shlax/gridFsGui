package org.gfs

import java.awt.event.{ActionEvent, ActionListener}
import java.awt.{Component, Container}
import javax.swing._

import scala.language.implicitConversions

object auto {

  implicit class AutoClose[A <: AutoCloseable](c:A){
    def autoClose[B](f : A => B ) = {
      try{ f(c)
      }finally { c.close() }
    }
  }
}

class UnitAction[T](f : => T) extends ActionListener{
  override def actionPerformed(e: ActionEvent){ f }
}

object autoGui{

  implicit class autoActionListener[T <: AbstractButton](b: T){
    def call(f : => Unit) = {
      b.addActionListener(unitAction(f))
      b
    }
    def event(f : => Unit) = {
      b.addActionListener(unitAction(f))
      b
    }
  }

  implicit class autoComponent(p:Container){
    def += [T <: Component](c: T) = {
      p.add(c)
      c
    }
    def += [T <: Component](c:T, const:Any) = {
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

  implicit def unitAction[T](f: => T)= new UnitAction(f)

}
