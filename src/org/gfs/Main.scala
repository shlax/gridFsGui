package org.gfs

import java.awt.event.{WindowAdapter, WindowEvent}
import javax.swing.{SwingUtilities, UIManager}

import org.gfs.gui.{ConnectDialog, Gui}

// -Xdisable-assertions
object Main extends App with Runnable{
  UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel")//(UIManager.getSystemLookAndFeelClassName)
  SwingUtilities.invokeLater(this)

  override def run(){
    val f = Gui()
    f.addWindowListener(new WindowAdapter(){
      override def windowClosed(e: WindowEvent){ Gfs.pull.shutdown() }
    })
    f.setVisible(true)
    ConnectDialog()
  }
}
