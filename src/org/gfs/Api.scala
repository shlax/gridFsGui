package org.gfs

import org.gfs.gui.{Command, Gui}
import org.gfs.mongo.ConnectionPull
import groovy.lang.Closure

object Api {

  def refresh(){
    Command.gui(Gui().refresh()).run()
  }

  // mongo api

  def mongoClient() = ConnectionPull()

  def gridFs(nm:String) = ConnectionPull.gridFs(if(nm == null || nm.isEmpty) None else Some(nm) )

  val lock = new Object
  var handlers:List[Closure[_]] = Nil

  /** org.gfs.Api.register{ t, f -> println(t+':'+f) } */
  def register(c:Closure[_]) = {
    lock.synchronized{ handlers = c :: handlers }
    c
  }

  def unRegister(c:Closure[_]){
    lock.synchronized{ handlers = handlers.filter(_ != c) }
  }

  def event(e:Object*){
    val l = lock.synchronized{ handlers }
    if(l.nonEmpty) Command.job { for (c <- l if c.getMaximumNumberOfParameters >= e.size) c.call(e:_*) }.run()
  }

}
