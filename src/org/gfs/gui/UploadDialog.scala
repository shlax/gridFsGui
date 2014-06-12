package org.gfs.gui

import java.awt.{BorderLayout, GridLayout}
import java.io.FileInputStream
import javax.swing._

import org.gfs.Command
import org.gfs.mongo.MongoFs

object UploadDialog{
  def apply(basePath:String, replace:Boolean = false){
    new UploadDialog(basePath, replace).open()
  }
}

class UploadDialog(basePath:String, replace:Boolean = false) extends OkCancelDialog{
  assert(SwingUtilities.isEventDispatchThread)

  import org.gfs.autoGui._

  val field = getContentPane += (new JPanel(new GridLayout(4, 1)), BorderLayout.NORTH)
  field.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5))

  field.add(new JLabel("from file:"))

  val fromPanel = field += new JPanel(new BorderLayout())

  val tfFile = fromPanel += new JTextField(40)
  val btOpen = fromPanel += (new JButton("open").call(choose()), BorderLayout.EAST)

  field.add(new JLabel("to gridFs:"))
  val tfPath = field += new JTextField(20)

  tfPath.setText(basePath+(if(replace) "" else "/"))

  override def ok(){
    assert(SwingUtilities.isEventDispatchThread)

    val from = tfFile.getText
    val to = tfPath.getText

    Command.job{
      if(replace) MongoFs.delete(to)
      MongoFs.put(to, new FileInputStream(from))
    }
  }

  def choose(){
    val fch = new JFileChooser()
    if (fch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) tfFile.setText(fch.getSelectedFile.getAbsolutePath)
  }

}
