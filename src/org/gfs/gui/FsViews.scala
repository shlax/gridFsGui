package org.gfs.gui

import org.gfs.mongo.GfsFile
import javax.swing.tree.{DefaultTreeModel, DefaultMutableTreeNode}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object FsMode{
  val modes = Array[FsMode](DirMode, ListMode, ExtDir, ExtList)
}

trait FsMode{
  def apply(f:GfsFile) : List[String]
}

case object ListMode extends FsMode{
  override def apply(f: GfsFile) = List(f.name)
  override def toString = "list"
}

case object DirMode extends FsMode{
  override def apply(f: GfsFile) = f.name.split("/").toList
  override def toString = "dir"
}

case object ExtDir extends FsMode{
  override def apply(f: GfsFile) = {
    val p = f.name.split("/").toList
    val i = p.last.lastIndexOf(".")
    if(i == -1) p else p.last.substring(i+1) :: p
  }
  override def toString = "ext/dir"
}

case object ExtList extends FsMode{
  override def apply(f: GfsFile) = {
    val i = f.name.lastIndexOf(".")
    if(i == -1) List(f.name) else List(f.name.substring(i+1), f.name)
  }
  override def toString = "ext/list"
}

class FsFile(val dir:String, val name:String, val file:Option[GfsFile] = None){
  override def toString = dir++name
}

object FsViews {

  class Node(nm:String){
    val childs = mutable.Map[String, Node]()
    val files = ListBuffer[GfsFile]()

    def apply(nm:List[String]) : Node = nm match {
      case Nil => this
      case h :: t => childs.getOrElseUpdate(h, new Node(h)).apply(t)
    }

    def add(f: GfsFile){ files += f }

    def node(parent:String) : DefaultMutableTreeNode = {
      if(childs.isEmpty){
        val n = new DefaultMutableTreeNode()
        if(files.length == 1) n.setUserObject(new FsFile(parent, nm, Some(files.head)))
        else {
          n.setUserObject(new FsFile(parent, nm))
          for (f <- files) n.add(new DefaultMutableTreeNode(new FsFile(parent,nm, Some(f))))
        }
        n
      }else if(files.isEmpty && childs.size == 1 && !nm.isEmpty){ // not root
        childs.head._2.node(parent+nm+"/")
      }else{
        val n = new DefaultMutableTreeNode()
        n.setUserObject(new FsFile(parent, nm))
        for(c <- childs.values) n.add(c.node(""))
        for (f <- files) n.add(new DefaultMutableTreeNode(new FsFile(parent, nm, Some(f))))
        n
      }
    }
  }

  def apply(fs:List[GfsFile], fm: FsMode ) = {
    val r = new Node("")
    for(f <- fs) r(fm(f)).add(f)
    (fs, new DefaultTreeModel(r.node("")))
  }

}
