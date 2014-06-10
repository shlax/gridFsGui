package org.gfs.gui

import org.gfs.mongo.MongoFs
import javax.swing._
import java.awt.BorderLayout
import org.gfs.{autoGui, Command}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import org.gfs.mongo.GfsFile
import scala.collection.mutable

class TabbedPane extends JTabbedPane{
  assert(SwingUtilities.isEventDispatchThread)

  val menu = new JPopupMenu()
  setComponentPopupMenu(menu)

  val closeItm = new JMenuItem("close")
  menu.add(closeItm)

  case class Tab(file:GfsFile, cmp:JTextArea)
  val opened  = mutable.ListBuffer[Tab]()

  def apply(f:GfsFile, replace:Int = -1) = {
    assert(SwingUtilities.isEventDispatchThread)

    val p = new JPanel(new BorderLayout())
    val ta = new JTextArea()
    p.add(new JScrollPane(ta))

    if(replace == -1) {
      opened += Tab(f, ta)
      add(f.name, p)
      setSelectedIndex(opened.length-1)
    }else{
      opened(replace) = Tab(f, ta)
      remove(replace)
      add(p, replace)
      setTitleAt(replace, f.name)
      setSelectedIndex(replace)
    }

    if(f.exist()) Command.job{
      val out = new ByteArrayOutputStream()
      MongoFs.load(f.name, out)
      out
    }.toGui{ out =>
      ta.setText(new String(out.toByteArray, "UTF-8"))
    }.run()
    this
  }

  def newTab() = apply(GfsFile("*new"))

  def saveTab(): TabbedPane = {
    assert(SwingUtilities.isEventDispatchThread)

    val ind = getSelectedIndex
    if(ind == -1) return this
    val t = opened(ind)
    if(t.file.exist()){
      val nm = t.file.name
      val in = new ByteArrayInputStream(t.cmp.getText.getBytes("UTF-8"))
      Command.job(MongoFs.replace(nm, in)).run()
    } else{
      val nm = JOptionPane.showInputDialog("file name")
      if(nm == null || nm.isEmpty) return this
      val in = new ByteArrayInputStream(t.cmp.getText.getBytes("UTF-8"))
      Command.job(MongoFs.put(nm, in)).gui(Gui().refresh(_.find(_.name == nm).foreach(apply(_, ind)))).run()
    }
    this
  }

  def close(): TabbedPane = {
    val ind = getSelectedIndex
    if(ind == -1) return this
    opened.remove(ind)
    remove(ind)
    this
  }

  import autoGui._

  closeItm.addActionListener(close())
}