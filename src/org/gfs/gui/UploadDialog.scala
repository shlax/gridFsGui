package org.gfs.gui

import javax.swing._
import java.awt.{BorderLayout, GridLayout}
import org.gfs.{Command, autoGui}
import org.gfs.mongo.MongoFs
import java.io.FileInputStream

object UploadDialog{
  def apply(basePath:String, replace:Boolean = false){
    new UploadDialog(basePath, replace).open()
  }
}

class UploadDialog(basePath:String, replace:Boolean = false) extends OkCancelDialog{
  assert(SwingUtilities.isEventDispatchThread)

  import autoGui._

  val field = getContentPane.apply(new JPanel(new GridLayout(4, 1)), BorderLayout.NORTH)
  field.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5))

  field.add(new JLabel("from file:"))

  val fromPanel = field(new JPanel(new BorderLayout()))

  val tfFile = fromPanel(new JTextField(40))
  val btOpen = fromPanel(new JButton("open"), BorderLayout.EAST).$(choose())

  field.add(new JLabel("to gridFs:"))
  val tfPath = field(new JTextField(20))

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
