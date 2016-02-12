package org.gfs.gui

import java.awt.BorderLayout
import javax.swing._

import org.gfs.mongo.{MongoFs, GfsFile}

class FsGui(val dbFs: MongoFs) extends JPanel{
  import org.gfs.gui.autoGui._

  setLayout(new BorderLayout())

  val bar = this += (new JToolBar(), BorderLayout.NORTH)
  bar.setFloatable(false)

  val mainSp = this += new JSplitPane(JSplitPane.VERTICAL_SPLIT)
  mainSp.setOneTouchExpandable(true)
  mainSp.setDividerLocation(0)

  val queryTf = new JTextArea()
  mainSp.setTopComponent(new JScrollPane(queryTf))

  val sp = new JSplitPane()
  sp.setOneTouchExpandable(true)
  sp.setDividerLocation(200)

  mainSp.setBottomComponent(sp)

  val left = new JPanel(new BorderLayout())
  sp.setLeftComponent(left)

  val fsView = left += (new JComboBox(FsMode.modes), BorderLayout.NORTH)
  fsView.addActionListener(reload())

  val tree = left.scroll(new Tree(this))

  val tabbedPane = new TabbedPane(this)
  sp.setRightComponent(tabbedPane)

  val newBt = bar += new JButton("new").call(tabbedPane.newTab())
  val saveBt = bar += new JButton("save").call(tabbedPane.saveTab())
  val uploadBt = bar += new JButton("upload").call(upload())

  val runBt = bar += new JButton("run").call(refresh())

  def reload(){
    assert(SwingUtilities.isEventDispatchThread)

    val fs = fsView.getSelectedItem.asInstanceOf[FsMode]
    val l = tree.files

    Command.job(FsViews.apply(l, fs)).toGui(tree.model).run()
  }

  def refresh(after: List[GfsFile] => Unit = { m => }) = {
    assert(SwingUtilities.isEventDispatchThread)

    val fs = fsView.getSelectedItem.asInstanceOf[FsMode]
    val q = queryTf.getText.trim()

    Command.job(FsViews.apply(dbFs.list(q), fs)).toGui{ l =>
      tree.model(l)
      after(l._1)
    }.run()
    this
  }

  def openFile(){
    assert(SwingUtilities.isEventDispatchThread)

    tree.selectedFile().foreach(tabbedPane(_))
  }


  def upload(){
    assert(SwingUtilities.isEventDispatchThread)

    UploadDialog(dbFs ,tree.selectedPath().filter(_.file.isEmpty).map(_.path).mkString("/"))
  }

}
