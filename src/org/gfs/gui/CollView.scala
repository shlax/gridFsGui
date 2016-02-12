package org.gfs.gui

import java.awt.event.{MouseEvent, MouseAdapter}
import java.awt.{FlowLayout, BorderLayout}
import javax.swing._
import javax.swing.event.{TreeExpansionEvent, TreeWillExpandListener}
import javax.swing.tree.{DefaultTreeModel, TreePath, DefaultMutableTreeNode}

import com.mongodb.{BasicDBObject, DBObject}
import com.mongodb.util.JSON
import groovy.json.JsonOutput
import org.gfs.mongo.ConnectionPull

class CollView(nm:String) extends JPanel with TreeWillExpandListener {
  import org.gfs.gui.autoGui._

  setLayout(new BorderLayout())

  val bar = this += (new JToolBar(), BorderLayout.NORTH)
  bar.setFloatable(false)

  val off = bar += new JTextField("0", 5)
  val lim = bar += new JTextField("50", 5)

  bar += new JButton("run").call(refresh())
  bar += new JButton("add").call(add())

  val mainSp = this += new JSplitPane(JSplitPane.VERTICAL_SPLIT)
  mainSp.setDividerLocation(75)
  mainSp.setOneTouchExpandable(true)

  val ta = mainSp.scroll(new JTextArea())

  val dtNod = new DefaultMutableTreeNode()
  val tree = mainSp.scroll(new JTree(dtNod))
  tree.setRootVisible(false)
  tree.addTreeWillExpandListener(this)

  tree.addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent){
      if(e.getClickCount == 2) open()
    }
  })

  def coll() = {
    assert(!SwingUtilities.isEventDispatchThread)
    ConnectionPull().getCollection(nm)
  }

  case class Nod(nm:String, v:Any){
    override def toString = nm+":"+v
  }

  def refresh(){
    assert(SwingUtilities.isEventDispatchThread)

    import scala.collection.JavaConversions._

    val q = ta.getText
    val of = off.getText().toInt
    val lm = lim.getText().toInt

    Command.job( (if(q.trim.isEmpty) coll().find() else coll().find(JSON.parse(q).asInstanceOf[DBObject])).skip(of).batchSize(lm).toArray().toList ).toGui{l =>
      dtNod.removeAllChildren()
      for(i <- l)dtNod.add(new DefaultMutableTreeNode(Nod(i.toString, i), true))
      expand(dtNod)
      tree.expandPath(new TreePath(dtNod))
      tree.getModel.asInstanceOf[DefaultTreeModel].reload(dtNod)
    }.run()
  }

  def open(){
    val p = tree.getSelectionPath
    if(p != null && p.getPath.length == 2) new JsonDialog(nm, Some(p.getLastPathComponent.asInstanceOf[DefaultMutableTreeNode].getUserObject.asInstanceOf[Nod].v.asInstanceOf[DBObject])).setVisible(true)
  }

  def add(){
    assert(SwingUtilities.isEventDispatchThread)
    new JsonDialog(nm).setVisible(true)
  }

  override def treeWillExpand(event: TreeExpansionEvent) {
    expand(event.getPath.getLastPathComponent.asInstanceOf[DefaultMutableTreeNode])
  }

  def expand(par: DefaultMutableTreeNode){
    import scala.collection.JavaConversions._
    (0 until par.getChildCount).foreach{ i =>
      val n = par.getChildAt(i).asInstanceOf[DefaultMutableTreeNode]
      n.getUserObject.asInstanceOf[Nod].v match {
        case dbo: DBObject =>
          for(e <- dbo.toMap) n.add(new DefaultMutableTreeNode(Nod(e._1.toString, e._2), true))
        case _ =>
      }
    }
  }

  override def treeWillCollapse(event: TreeExpansionEvent){
    val par = event.getPath.getLastPathComponent.asInstanceOf[DefaultMutableTreeNode]
    (0 until par.getChildCount).foreach(i => par.getChildAt(i).asInstanceOf[DefaultMutableTreeNode].removeAllChildren())
  }
}

class JsonDialog(col:String, o:Option[DBObject]=None) extends JDialog(Gui()){
  assert(SwingUtilities.isEventDispatchThread)
  setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)

  import org.gfs.gui.autoGui._

  setLayout(new BorderLayout())

  val ta = this.scroll(new JTextArea(o.map(j => JsonOutput.prettyPrint(JSON.serialize(j))).getOrElse("")))

  val actP = this += (new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5)), BorderLayout.SOUTH)
  actP += new JButton("save").call(save())

  setSize(500, 400)
  setLocationRelativeTo(Gui())

  def save(){
    val t = ta.getText
    Command.job{
      if(o.isEmpty) ConnectionPull().getCollection(col).insert( JSON.parse(t).asInstanceOf[DBObject] )
      else ConnectionPull().getCollection(col).update(new BasicDBObject("_id", o.get.get("_id")), JSON.parse(t).asInstanceOf[DBObject] )
    }.inGui{
      setVisible(false)
      dispose()
    }.run()
  }
}
