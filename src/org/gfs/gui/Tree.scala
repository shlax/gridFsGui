package org.gfs.gui

import java.awt.event._
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import javax.swing._
import javax.swing.tree.{DefaultMutableTreeNode, DefaultTreeCellRenderer, DefaultTreeModel, TreePath}

import org.gfs.mongo.{GfsFile, MongoFs}

import scala.collection.JavaConversions.enumerationAsScalaIterator
import scala.language.implicitConversions

class Tree extends JTree(new DefaultTreeModel(new DefaultMutableTreeNode())){
  assert(SwingUtilities.isEventDispatchThread)

  setRootVisible(false)

  var files: List[GfsFile] = Nil

  val menu = new JPopupMenu()
  setComponentPopupMenu(menu)

  import org.gfs.gui.autoGui._

  val deleteMi = menu += new JMenuItem("Delete").call(delete())
  val downloadMi = menu += new JMenuItem("Download").call(download())
  val uploadMi = menu += new JMenuItem("Upload").call(upload())

  deleteMi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0))

  this.addKeyListener(new KeyAdapter(){
    override def keyPressed(e: KeyEvent){
      if(e.getKeyCode == KeyEvent.VK_DELETE) delete()
    }
  })

  implicit def toFs(a:Any) = a.asInstanceOf[DefaultMutableTreeNode].getUserObject.asInstanceOf[FsFile]

  def selectedPath() = {
    assert(SwingUtilities.isEventDispatchThread)

    val p = getSelectionPath
    if(p == null) Nil else p.getPath.toList.tail.map(toFs)
  }

  def selectedFile() = {
    assert(SwingUtilities.isEventDispatchThread)

    val p = getSelectionPath
    if(p == null) None else p.getLastPathComponent.file
  }

  def model(m:(List[GfsFile], DefaultTreeModel)){
    assert(SwingUtilities.isEventDispatchThread)

    val ep = getExpandedDescendants(new TreePath(getModel.getRoot))
    val toExpand = if(ep == null) Nil else ep.toList.map(_.getPath.toList.map(_.toString))
    files = m._1
    setModel(m._2)

    def lookUp(n:DefaultMutableTreeNode, p:List[String]) : List[DefaultMutableTreeNode] = p match {
      case Nil => Nil
      case h :: t =>
        val c = n.children().find(_.toString == h)
        if(c.isDefined){
          val n = c.get.asInstanceOf[DefaultMutableTreeNode]
          n :: lookUp(n, t)
        }else Nil
    }

    val root = m._2.getRoot.asInstanceOf[DefaultMutableTreeNode]
    for(p <- toExpand if p.size > 1){
      val tp = lookUp(root, p.tail)
      if(tp.nonEmpty) expandPath(new TreePath((root :: tp).toArray[AnyRef]))
    }
  }

  ToolTipManager.sharedInstance().registerComponent(this)
  setCellRenderer(new DefaultTreeCellRenderer(){
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    override def getTreeCellRendererComponent(tree: JTree, value: scala.Any, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean) = {
      val f = value.asInstanceOf[DefaultMutableTreeNode].getUserObject.asInstanceOf[FsFile]
      if(f.file.isDefined) setToolTipText(
        s"""<html><body>
          |${f.file.get.name}<br>
          |${f.file.get.length}<br>
          |${format.format(f.file.get.uploadDate)}
        </body></html>""".stripMargin.trim) else setToolTipText(f.path)
      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)
    }
  })

  def delete(){
    assert(SwingUtilities.isEventDispatchThread)

    selectedFile().foreach{f =>
      if(JOptionPane.showConfirmDialog(Gui(), f.name, "delete", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) Command.job(MongoFs.delete(f.name)).gui(Gui().refresh()).run()
    }
  }

  def download(){
    assert(SwingUtilities.isEventDispatchThread)

    selectedFile().foreach{ f =>
      val fch = new JFileChooser()
      if (fch.showSaveDialog(Gui()) == JFileChooser.APPROVE_OPTION) {
        val sf = fch.getSelectedFile
        Command.job(MongoFs.load(f.name, new FileOutputStream(sf))).run()
      }
    }
  }

  def upload(){
    assert(SwingUtilities.isEventDispatchThread)

    selectedFile().foreach(f => UploadDialog(f.name, true))
  }

  addMouseListener(new MouseAdapter{
    override def mouseClicked(e: MouseEvent){
      if(e.getClickCount == 2) Gui().openFile()
    }
  })

}
