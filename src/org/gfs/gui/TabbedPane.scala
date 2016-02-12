package org.gfs.gui

import java.awt.{Color, BorderLayout}
import java.awt.event.{ActionEvent, InputEvent, KeyEvent}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import javax.swing._
import javax.swing.text.{DefaultHighlighter, JTextComponent}
import javax.swing.undo.UndoManager

import org.gfs.mongo.GfsFile

import scala.collection.mutable

class TabbedPane(fsGui:FsGui) extends ClosablePane{

  import org.gfs.gui.autoGui._

  case class Tab(file:GfsFile, cmp:JTextArea)
  val opened  = mutable.ListBuffer[Tab]()

  def apply(f:GfsFile, replace:Int = -1) {
    assert(SwingUtilities.isEventDispatchThread)

    val ei = opened.indexWhere(_.file.name == f.name)
    if (ei != -1) setSelectedIndex(ei)
    else{
      val ind = f.name.lastIndexOf("/")
      val title = if (ind == -1) f.name else f.name.substring(ind + 1)

      val p = new JPanel(new BorderLayout())
      val ta = p.scroll(new JTextArea())
      ta.setTabSize(1)

      val sTf = p +=(new JTextField(), BorderLayout.NORTH)
      sTf.setVisible(false)
      sTf.addActionListener(unitAction {
        val ha = ta.getHighlighter
        ha.removeAllHighlights()
        val t = sTf.getText

        if (!t.isEmpty) {
          val hp = new DefaultHighlighter.DefaultHighlightPainter(Color.GRAY)
          for (i <- t.r.findAllMatchIn(ta.getText)) ha.addHighlight(i.start, i.end, hp)
        }
      })

      val sm = JTextComponent.addKeymap("tabbedPaneSearch", sTf.getKeymap)
      sm.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), new AbstractAction() {
        override def actionPerformed(e: ActionEvent) {
          ta.getHighlighter.removeAllHighlights()
          sTf.setVisible(false)
          p.revalidate()
          ta.requestFocus()
        }
      })
      sTf.setKeymap(sm)

      val undo = new UndoManager()
      ta.getDocument.addUndoableEditListener(undo)

      val saveAct = new AbstractAction("Save") {
        override def actionPerformed(e: ActionEvent) {
          saveTab()
        }
      }
      val undoAct = new AbstractAction("Undo") {
        override def actionPerformed(e: ActionEvent) {
          if (undo.canUndo) undo.undo()
        }
      }
      val redoAct = new AbstractAction("Redo") {
        override def actionPerformed(e: ActionEvent) {
          if (undo.canRedo) undo.redo()
        }
      }
      val findAct = new AbstractAction("Find") {
        override def actionPerformed(e: ActionEvent) {
          sTf.setVisible(true)
          p.revalidate()
          sTf.requestFocus()
        }
      }

      val ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK)
      val ctrlZ = KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK)
      val ctrlY = KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK)
      val ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK)

      val km = JTextComponent.addKeymap("tabbedPaneEditor", ta.getKeymap)

      km.addActionForKeyStroke(ctrlS, saveAct)
      km.addActionForKeyStroke(ctrlZ, undoAct)
      km.addActionForKeyStroke(ctrlY, redoAct)
      km.addActionForKeyStroke(ctrlF, findAct)

      ta.setKeymap(km)

      val pop = new JPopupMenu()
      ta.setComponentPopupMenu(pop)

      (pop += new JMenuItem(saveAct)).setAccelerator(ctrlS)
      (pop += new JMenuItem(undoAct)).setAccelerator(ctrlZ)
      (pop += new JMenuItem(redoAct)).setAccelerator(ctrlY)
      (pop += new JMenuItem(findAct)).setAccelerator(ctrlF)

      if (replace == -1) {
        opened += Tab(f, ta)
        add(title, p)
        setTabComponentAt(opened.size - 1, new ClosableTab())

        setSelectedIndex(opened.length - 1)
      } else {
        opened(replace) = Tab(f, ta)
        remove(replace)
        add(p, replace)
        setTabComponentAt(replace, new ClosableTab())

        setTitleAt(replace, title)
        setSelectedIndex(replace)
      }

      if (f.exist()) Command.job {
        val out = new ByteArrayOutputStream()
        fsGui.dbFs.load(f.name, out)
        out
      }.toGui { out =>
        ta.setText(new String(out.toByteArray, "UTF-8"))
      }.run()

    }
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
      Command.job(fsGui.dbFs.replace(nm, in)).run()
    } else{
      val nm = JOptionPane.showInputDialog("file name")
      if(nm == null || nm.isEmpty) return
      val in = new ByteArrayInputStream(t.cmp.getText.getBytes("UTF-8"))
      Command.job(fsGui.dbFs.put(nm, in)).gui(fsGui.refresh(_.find(_.name == nm).foreach(apply(_, ind)))).run()
    }
  }

  override def removeTab(i:Int){
    assert(SwingUtilities.isEventDispatchThread)

    opened.remove(i)
  }

}
