package org.gfs.gui

import org.gfs.mongo.ConnectionPull
import org.gfs.mongo.MongoFs

class QueryPane extends ClosablePane {

  def add(nm:String){
    val v = new CollView(nm)
    addTab(nm, v)
    setTabComponentAt(getTabCount - 1, new ClosableTab())
    setSelectedIndex(getTabCount - 1)
//    v.refresh()
  }

  def addFs(nm:String){
    Command.job(new MongoFs(ConnectionPull.gridFs(Some(nm)))).toGui{ f =>
      val fs = new FsGui(f)
      addTab(nm, fs)
      setTabComponentAt(getTabCount - 1, new ClosableTab())
      setSelectedIndex(getTabCount - 1)
      fs.refresh()
    }.run()
  }

}
