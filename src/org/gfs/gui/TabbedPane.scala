package org.gfs.gui

import java.awt.BorderLayout
import java.awt.event.{ActionEvent, InputEvent, KeyEvent}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import javax.swing._
import javax.swing.text.JTextComponent
import javax.swing.undo.UndoManager

import org.gfs.mongo.{GfsFile, MongoFs}

import scala.collection.mutable

class TabbedPane extends JTabbedPane{
  assert(SwingUtilities.isEventDispatchThread)

  val menu = new JPopupMenu()
  setComponentPopupMenu(menu)

  import org.gfs.gui.autoGui._

  val closeItm = menu += new JMenuItem("close").call(closeTab())

  case class Tab(file:GfsFile, cmp:JTextArea)
  val opened  = mutable.ListBuffer[Tab]()

  def apply(f:GfsFile, replace:Int = -1){
    assert(SwingUtilities.isEventDispatchThread)

    val ind = f.name.lastIndexOf("/")
    val title = if(ind == -1) f.name else f.name.substring(ind+1)

    val p = new JPanel(new BorderLayout())
    val ta = p.scroll(new JTextArea())
    ta.setTabSize(1)

    val undo = new UndoManager()
    ta.getDocument.addUndoableEditListener(undo)

    val saveAct = new AbstractAction("Save"){
      override def actionPerformed(e: ActionEvent){ saveTab() }
    }
    val undoAct = new AbstractAction("Undo"){
      override def actionPerformed(e: ActionEvent){ if(undo.canUndo) undo.undo() }
    }
    val redoAct = new AbstractAction("Redo"){
      override def actionPerformed(e: ActionEvent){ if(undo.canRedo) undo.redo() }
    }

    val ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK)
    val ctrlZ = KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK)
    val ctrlY = KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK)

    val km = JTextComponent.addKeymap("tabbedPaneEditor", ta.getKeymap)

    km.addActionForKeyStroke(ctrlS, saveAct)
    km.addActionForKeyStroke(ctrlZ, undoAct)
    km.addActionForKeyStroke(ctrlY, redoAct)

    ta.setKeymap(km)

    val pop = new JPopupMenu()
    ta.setComponentPopupMenu(pop)

    (pop += new JMenuItem(saveAct)).setAccelerator(ctrlS)
    (pop += new JMenuItem(undoAct)).setAccelerator(ctrlZ)
    (pop += new JMenuItem(redoAct)).setAccelerator(ctrlY)

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
    assert(SwingUtilities.isEventDispatchThread)

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

  def closeTab(){
    assert(SwingUtilities.isEventDispatchThread)

    val ind = getSelectedIndex
    if(ind == -1) return
    opened.remove(ind)
    remove(ind)
  }

}
