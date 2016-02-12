package org.gfs.gui

import java.awt.BorderLayout
import javax.swing._

abstract class ClosablePane extends JTabbedPane{
  assert(SwingUtilities.isEventDispatchThread)

  import org.gfs.gui.autoGui._

  val menu = new JPopupMenu()
  setComponentPopupMenu(menu)

  menu += new JMenuItem("close").call(closeTab()) // val closeItm =

  def removeTab(i:Int){}

  def closeTab(i:Int = -1){
    assert(SwingUtilities.isEventDispatchThread)

    val ind = if(i != -1) i else getSelectedIndex
    if(ind == -1) return
    removeTab(ind)
    remove(ind)
  }

  class ClosableTab extends JPanel(new BorderLayout()){
    setOpaque(false)
    add(new JLabel(){
      setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2))
      override def getText = {
        val i = ClosablePane.this.indexOfTabComponent(ClosableTab.this)
        if(i == -1) "" else ClosablePane.this.getTitleAt(i)
      }
    })
    add(new JButton("X"){
      setBorder(BorderFactory.createEmptyBorder(2,5,2,5))
    }.call{
      val i = ClosablePane.this.indexOfTabComponent(ClosableTab.this)
      if(i != -1) closeTab(i)
    }, BorderLayout.EAST)
  }

}
