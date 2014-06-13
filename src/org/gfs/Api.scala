package org.gfs

import org.gfs.gui.{Command, Gui}
import org.gfs.mongo.ConnectionPull

object Api {

  def newTab(){
    Command.gui(Gui().tabbedPane.newTab()).run()
  }

  def closeTab(){
    Command.gui(Gui().tabbedPane.closeTab()).run()
  }

  def refresh(){
    Command.gui(Gui().refresh()).run()
  }

  // mongo api

  def mongoClient() = ConnectionPull()

  def gridFs() = ConnectionPull.gridFs()

}
