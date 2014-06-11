package org.gfs.gui

import javax.swing._
import java.awt._
import org.gfs.mongo.ConnectionPull
import org.gfs.autoGui

class ConnectDialog extends JDialog(Gui()){
  assert(SwingUtilities.isEventDispatchThread)

  setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
  setModal(true)

  val field = new JPanel(new GridLayout(2, 2))
  getContentPane.add(field, BorderLayout.NORTH)

  val lHost = new JLabel("host/port:")
  val lDb = new JLabel("db/bucket:")


  val tfHost = new JTextField(20)
  val tfPort = new JTextField(7)

  val tfDb = new JTextField(20)
  val tfBucket = new JTextField(7)

  // ConnectionPull.connect("denoviusapp1.hq.gratex.com", 40000, "test-test", "")
  tfHost.setText("denoviusapp1.hq.gratex.com")
  tfPort.setText("40000")
  tfDb.setText("test-test")
  tfBucket.setText("")

  field.setLayout(new GridBagLayout())
  def addField(c:JComponent, x:Int, y:Int, w:Double){
    val gc = new GridBagConstraints()
    gc.fill = GridBagConstraints.HORIZONTAL
    gc.weightx = w; gc.gridx = x; gc.gridy = y
    gc.insets = new Insets(if(y == 0) 10 else 3 ,3,3,3)
    field.add(c, gc)
  }

  addField(lHost, 0, 0, 0.25)
  addField(lDb, 0, 1, 0.25)

  addField(tfHost, 1, 0, 0.5)
  addField(tfDb, 1, 1, 0.5)

  addField(tfPort, 2, 0, 0.24)
  addField(tfBucket, 2, 1, 0.24)

  val buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT))
  getContentPane.add(buttons, BorderLayout.SOUTH)

  val okButton = new JButton("ok")
  getRootPane.setDefaultButton(okButton)

  val cancelButton = new JButton("cancel")

  buttons.add(okButton)
  buttons.add(cancelButton)

  pack()
  setLocationRelativeTo(null)

  def doClose() = {
    setVisible(false)
    dispose()
    this
  }

  import autoGui._

  okButton.addActionListener(${
    ConnectionPull.connect(tfHost.getText, tfPort.getText.toInt, tfDb.getText, tfBucket.getText)
    Gui().refresh()
    doClose()
  })

  cancelButton.addActionListener(doClose())

}
