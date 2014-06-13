package org.gfs.gui

import java.awt.event.{ActionEvent, ActionListener}
import java.awt.{Component, Container}
import javax.swing.{AbstractButton, JScrollPane}

import scala.language.implicitConversions

class UnitAction[T](f : => T) extends ActionListener{
  override def actionPerformed(e: ActionEvent){ f }
}

object autoGui{

  implicit class autoActionListener[T <: AbstractButton](b: T){
    def call[Q](f : => Q) = {
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
