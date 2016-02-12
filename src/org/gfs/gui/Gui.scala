package org.gfs.gui

import java.awt.event.{KeyEvent, KeyAdapter, MouseEvent, MouseAdapter}
import javax.swing._

import com.mongodb.BasicDBObject
import org.gfs.mongo.ConnectionPull

object Gui{
  lazy val gui = new Gui()
  def apply() = gui
}

class Gui extends JFrame{
  assert(SwingUtilities.isEventDispatchThread)
  setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)

  import org.gfs.gui.autoGui._

  val menu = new JMenuBar()
  setJMenuBar(menu)

  val dbMn = menu += new JMenu("File")

  val reconnectMi = dbMn += new JMenuItem("Reconnect").call(reconnect())
  val groovyMi = dbMn += new JMenuItem("Groovy").call( Command.job(new groovy.ui.Console().run()).run() )

  val mainSp = this += new JSplitPane(JSplitPane.HORIZONTAL_SPLIT)
  mainSp.setDividerLocation(150)
  mainSp.setOneTouchExpandable(true)

  val colsNm = mainSp.scroll(new JList[String]())

  colsNm.addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent){
      if(e.getClickCount == 2) listOpen()
    }
  })

  colsNm.addKeyListener(new KeyAdapter {
    override def keyPressed(e: KeyEvent){
      if(e.getKeyCode == KeyEvent.VK_ENTER) listOpen()
    }
  })

  val qPane = mainSp += new QueryPane

  def listOpen(){
    val nm = colsNm.getSelectedValue
    if(nm != null && nm.nonEmpty){
      if(nm.endsWith(".files")) qPane.addFs(nm.substring(0, nm.length - 6))
      else qPane.add(nm)
    }
  }

  val colPop = new JPopupMenu()
  colsNm.setComponentPopupMenu(colPop)

  colPop += new JMenuItem("New collection").call{
    val nm = JOptionPane.showInputDialog("Name")
    if(nm != null && nm.nonEmpty) Command.job{
      val c = ConnectionPull().createCollection(nm, null)
      val o = new BasicDBObject()
      c.insert(o); c.remove(o) //crap
    }.inGui(refresh()).run()
  }

  setSize(800, 600)
  setLocationRelativeTo(null)

  def reconnect(){
    assert(SwingUtilities.isEventDispatchThread)

    DbConnectDialog()
  }

  def refresh(){
    assert(SwingUtilities.isEventDispatchThread)

    import scala.collection.JavaConversions._
    Command.job(ConnectionPull.colsNm().toList).toGui(i => colsNm.setListData(i.toArray)).run()
  }
}
