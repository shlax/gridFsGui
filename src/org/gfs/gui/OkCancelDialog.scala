package org.gfs.gui

import javax.swing._
import java.awt.{BorderLayout, FlowLayout}
import org.gfs.autoGui

abstract class OkCancelDialog extends JDialog(Gui()){
  assert(SwingUtilities.isEventDispatchThread)

  setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
  setModal(true)

  import autoGui._

  val buttons = getContentPane.apply(new JPanel(new FlowLayout(FlowLayout.RIGHT)), BorderLayout.SOUTH)

  val okButton = buttons(new JButton("ok")).${
    ok()
    doClose()
  }

  getRootPane.setDefaultButton(okButton)

  val cancelButton = buttons(new JButton("cancel")).$(doClose())

  def ok()

  def doClose(){
    assert(SwingUtilities.isEventDispatchThread)

    setVisible(false)
    dispose()
  }

  def open(){
    assert(SwingUtilities.isEventDispatchThread)

    pack()
    setLocationRelativeTo(null)
    setVisible(true)
  }

}
