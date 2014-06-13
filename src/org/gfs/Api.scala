package org.gfs

import org.gfs.gui.{Command, Gui}
import org.gfs.mongo.ConnectionPull

object Api {

  def refresh(){
    Command.gui(Gui().refresh()).run()
  }

  // mongo api

  def mongoClient() = ConnectionPull()

  def gridFs() = ConnectionPull.gridFs()

}
