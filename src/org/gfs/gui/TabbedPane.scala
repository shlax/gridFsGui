package org.gfs.gui

import java.awt.BorderLayout
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import javax.swing._

import org.gfs.Command
import org.gfs.mongo.{GfsFile, MongoFs}

import scala.collection.mutable

class TabbedPane extends JTabbedPane{
  assert(SwingUtilities.isEventDispatchThread)

  val menu = new JPopupMenu()
  setComponentPopupMenu(menu)

  import org.gfs.autoGui._

  val closeItm = menu += new JMenuItem("close").call(close())

  case class Tab(file:GfsFile, cmp:JTextArea)
  val opened  = mutable.ListBuffer[Tab]()

  def apply(f:GfsFile, replace:Int = -1){
    assert(SwingUtilities.isEventDispatchThread)

    val ind = f.name.lastIndexOf("/")
    val title = if(ind == -1) f.name else f.name.substring(ind+1)

    val p = new JPanel(new BorderLayout())
    val ta = p.scroll(new JTextArea())
    ta.setTabSize(1)

    if(replace == -1) {
      opened += Tab(f, ta)
      add(title, p)
      setSelectedIndex(opened.length-1)
    }else{
      opened(replace) = Tab(f, ta)
      remove(replace)
      add(p, replace)
      setTitleAt(replace, title)
      setSelectedIndex(replace)
    }

    if(f.exist()) Command.job{
      val out = new ByteArrayOutputStream()
      MongoFs.load(f.name, out)
      out
    }.toGui{ out =>
      ta.setText(new String(out.toByteArray, "UTF-8"))
    }.run()
  }

  def newTab(){
    apply(GfsFile("*new"))
  }

  def saveTab(){
    assert(SwingUtilities.isEventDispatchThread)

    val ind = getSelectedIndex
    if(ind == -1) return
    val t = opened(ind)
    if(t.file.exist()){
      val nm = t.file.name
      val in = new ByteArrayInputStream(t.cmp.getText.getBytes("UTF-8"))
      Command.job(MongoFs.replace(nm, in)).run()
    } else{
      val nm = JOptionPane.showInputDialog("file name")
      if(nm == null || nm.isEmpty) return
      val in = new ByteArrayInputStream(t.cmp.getText.getBytes("UTF-8"))
      Command.job(MongoFs.put(nm, in)).gui(Gui().refresh(_.find(_.name == nm).foreach(apply(_, ind)))).run()
    }
  }

  def close(){
    val ind = getSelectedIndex
    if(ind == -1) return
    opened.remove(ind)
    remove(ind)
  }

}
