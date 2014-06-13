package org.gfs

import java.awt.event.{WindowAdapter, WindowEvent}
import javax.swing.{SwingUtilities, UIManager}

import org.gfs.gui.{Command, ConnectDialog, Gui}
import org.gfs.mongo.ConnectionPull

// -Xdisable-assertions
object Main extends App with Runnable{
  UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel")//(UIManager.getSystemLookAndFeelClassName)
  SwingUtilities.invokeLater(this)

  override def run(){
    val f = Gui()
    f.addWindowListener(new WindowAdapter(){
      override def windowClosed(e: WindowEvent){
        Command.pull.shutdown()
        ConnectionPull.close()
      }
    })
    f.setVisible(true)
    ConnectDialog()
  }
}
