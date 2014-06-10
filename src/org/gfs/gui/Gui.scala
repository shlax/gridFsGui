package org.gfs.gui

import javax.swing._
import java.awt.BorderLayout
import org.gfs.{Command, autoGui}
import org.gfs.mongo.{GfsFile, MongoFs}

object Gui{
  lazy val gui = new Gui()
  def apply() = gui
}

class Gui extends JFrame{
  assert(SwingUtilities.isEventDispatchThread)
  setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)

  val menu = new JMenuBar()
  setJMenuBar(menu)

  val dbMn = new JMenu("DB")
  menu.add(dbMn)

  val reconnectMi = new JMenuItem("reconnect")
  dbMn.add(reconnectMi)

  val bar = new JToolBar()
  bar.setFloatable(false)
  getContentPane.add(bar, BorderLayout.NORTH)

  val newBt = new JButton("new")
  bar.add(newBt)

  val saveBt = new JButton("save")
  bar.add(saveBt)

  val refreshBt = new JButton("refresh")
  bar.add(refreshBt)

  val sp = new JSplitPane()
  sp.setDividerLocation(200)
  getContentPane.add(sp)

  val left = new JPanel()
  left.setLayout(new BorderLayout())
  val fsView = new JComboBox(FsMode.modes)
  left.add(fsView, BorderLayout.NORTH)

  val tree = new Tree()

  left.add(new JScrollPane(tree.tree))

  sp.setLeftComponent(left)

  val tabbedPane = new TabbedPane()
  sp.setRightComponent(tabbedPane.tabbedPane)

  setSize(800, 600)
  setLocationRelativeTo(null)

  def reload() = {
    assert(SwingUtilities.isEventDispatchThread)

    val fs = fsView.getSelectedItem.asInstanceOf[FsMode]
    val l = tree.files

    Command.job(FsViews.apply(l, fs)).toGui(Gui().tree.model).run()
    this
  }

  def refresh(after: List[GfsFile] => Unit = { m => }) = {
    assert(SwingUtilities.isEventDispatchThread)

    val fs = fsView.getSelectedItem.asInstanceOf[FsMode]
    Command.job(FsViews.apply(MongoFs.list(), fs)).toGui{ l =>
      tree.model(l)
      after(l._1)
    }.run()
    this
  }

  def openFile() = {
    assert(SwingUtilities.isEventDispatchThread)

    tree.selected().foreach(Gui().tabbedPane(_))
    this
  }

  def reconnect() = {
    assert(SwingUtilities.isEventDispatchThread)

    new ConnectDialog().setVisible(true)
    this
  }

  import autoGui._

  fsView.addActionListener(reload())

  newBt.addActionListener(tabbedPane.newTab())
  saveBt.addActionListener(tabbedPane.saveTab())

  refreshBt.addActionListener(refresh())
  reconnectMi.addActionListener(reconnect())
}
