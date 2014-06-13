package org.gfs.gui

import java.awt.{BorderLayout, FlowLayout}
import javax.swing._

abstract class OkCancelDialog extends JDialog(Gui()){
  assert(SwingUtilities.isEventDispatchThread)

  setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
  setModal(true)

  import org.gfs.gui.autoGui._

  val buttons = getContentPane += (new JPanel(new FlowLayout(FlowLayout.RIGHT)), BorderLayout.SOUTH)

  val okButton = buttons += new JButton("ok").call{
    ok()
    doClose()
  }

  getRootPane.setDefaultButton(okButton)

  val cancelButton = buttons += new JButton("cancel").call(doClose())

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
