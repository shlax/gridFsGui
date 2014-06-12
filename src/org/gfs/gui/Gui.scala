package org.gfs.gui

import java.awt.BorderLayout
import javax.swing._

import org.gfs.Command
import org.gfs.mongo.{GfsFile, MongoFs}

object Gui{
  lazy val gui = new Gui()
  def apply() = gui
}

class Gui extends JFrame{
  assert(SwingUtilities.isEventDispatchThread)
  setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)

  import org.gfs.autoGui._

  val menu = new JMenuBar()
  setJMenuBar(menu)

  val dbMn = menu += new JMenu("DB")

  val reconnectMi = dbMn += new JMenuItem("reconnect").call(reconnect())

  val bar = getContentPane += (new JToolBar(), BorderLayout.NORTH)
  bar.setFloatable(false)

  val mainPane = getContentPane += new JPanel(new BorderLayout())

  val sp = mainPane += new JSplitPane()
  sp.setOneTouchExpandable(true)
  sp.setDividerLocation(200)

  val left = new JPanel(new BorderLayout())
  sp.setLeftComponent(left)

  val fsView = left += (new JComboBox(FsMode.modes), BorderLayout.NORTH)
  fsView.addActionListener(reload())

  val tree = left.scroll(new Tree())

  val tabbedPane = new TabbedPane()
  sp.setRightComponent(tabbedPane)

  val newBt = bar += new JButton("new").call(tabbedPane.newTab())
  val saveBt = bar += new JButton("save").call(tabbedPane.saveTab())
  val uploadBt = bar += new JButton("upload").call(upload())

  val refreshBt = bar += new JButton("refresh").call(refresh())

  val queryPane = mainPane += (new JPanel(new BorderLayout()), BorderLayout.NORTH)

  val queryTf = queryPane += new JTextField()
  queryTf.addActionListener(runAction())

  val runBt = queryPane += (new JButton("run").call(runAction()), BorderLayout.EAST)

  setSize(800, 600)
  setLocationRelativeTo(null)

  def reload(){
    assert(SwingUtilities.isEventDispatchThread)

    val fs = fsView.getSelectedItem.asInstanceOf[FsMode]
    val l = tree.files

    Command.job(FsViews.apply(l, fs)).toGui(Gui().tree.model).run()
  }

  var query : String = ""

  def refresh(after: List[GfsFile] => Unit = { m => }) = {
    assert(SwingUtilities.isEventDispatchThread)

    val fs = fsView.getSelectedItem.asInstanceOf[FsMode]
    val q = query

    Command.job(FsViews.apply(MongoFs.list(q), fs)).toGui{ l =>
      tree.model(l)
      after(l._1)
    }.run()
    this
  }

  def openFile(){
    assert(SwingUtilities.isEventDispatchThread)

    tree.selectedFile().foreach(Gui().tabbedPane(_))
  }

  def reconnect(){
    assert(SwingUtilities.isEventDispatchThread)

    ConnectDialog()
  }

  def upload(){
    assert(SwingUtilities.isEventDispatchThread)

    UploadDialog(tree.selectedPath().filter(_.file.isEmpty).map(_.path).mkString("/"))
  }

  def runAction(){
    query = queryTf.getText.trim
    refresh()
  }
}
