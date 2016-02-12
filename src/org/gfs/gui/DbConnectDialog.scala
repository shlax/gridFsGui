package org.gfs.gui

import java.awt.{Insets, GridBagConstraints, BorderLayout, GridBagLayout}
import javax.swing._

import org.gfs.mongo.ConnectionPull

object DbConnectDialog{
  def apply(){
    new DbConnectDialog().open()
  }
}

class DbConnectDialog extends OkCancelDialog{

  assert(SwingUtilities.isEventDispatchThread)

  val field = new JPanel(new GridBagLayout())
  getContentPane.add(field, BorderLayout.NORTH)

  def addField[T <: JComponent](c:T, x:Int, y:Int, w:Double, gridwidth:Int = 1) = {
    val gc = new GridBagConstraints()
    gc.fill = GridBagConstraints.HORIZONTAL
    gc.gridwidth = gridwidth
    gc.weightx = w; gc.gridx = x; gc.gridy = y
    gc.insets = new Insets(if(y == 0) 10 else 3 ,3,3,3)
    field.add(c, gc)
    c
  }

  val lHost = addField(new JLabel("host/port:"), 0, 0, 0.25)
  val lDb = addField(new JLabel("db/bucket:"), 0, 1, 0.25)

  val tfHost = addField(new JTextField(20), 1, 0, 0.5)
  val tfPort = addField(new JTextField(7), 2, 0, 0.24)

  val tfDb = addField(new JTextField(20), 1, 1, 1, 2)

  // ConnectionPull.connect("denoviusapp1.hq.gratex.com", 40000, "test-test", "")
  tfHost.setText("mngappsrv1.hq.gratex.com")
  tfPort.setText("40000")
  tfDb.setText("test")

  override def ok(){
    assert(SwingUtilities.isEventDispatchThread)

    val h = tfHost.getText
    val p = tfPort.getText.toInt
    val db = tfDb.getText

    Command.job( ConnectionPull.connect(h, p, db) ).toGui(_ => Gui().refresh()).run()
  }

}
