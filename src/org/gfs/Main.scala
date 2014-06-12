package org.gfs

import javax.swing.{UIManager, SwingUtilities}
import org.gfs.gui.{ConnectDialog, Gui}
import java.awt.event.{WindowEvent, WindowAdapter}

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
