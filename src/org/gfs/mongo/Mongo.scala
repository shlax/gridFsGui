package org.gfs.mongo

import java.io.{InputStream, OutputStream}
import java.util.Date
import javax.swing.SwingUtilities

import com.mongodb.gridfs.GridFS
import com.mongodb.util.JSON
import com.mongodb.{BasicDBObject, DBObject, MongoClient, WriteConcern}
import org.gfs.auto._

import scala.collection.JavaConversions.asScalaIterator

object ConnectionPull {
  val lock = new Object
  var pull : Option[MongoClient] = None

  var dbNm : Option[String] = None
  var dbBucket : Option[String] = None

  def apply() = lock.synchronized{ pull.get }

  def connect(host:String, port:Int, db:String, bucket:String) {
    lock.synchronized {
      pull = Some(new MongoClient(host, port))
      dbNm = if(db.isEmpty) None else Some(db)
      dbBucket = if(bucket.isEmpty) None else Some(bucket)
    }
  }

  def gridFs() = lock.synchronized {
    val db = pull.get.getDB(dbNm.get)
    db.setWriteConcern(WriteConcern.REPLICAS_SAFE)
    if (dbBucket.isEmpty) new GridFS(db) else new GridFS(db, dbBucket.get)
  }

}

object GfsFile{
  def apply(name:String) = new GfsFile(name)
}

case class GfsFile(name:String, length:Long, uploadDate:Date){
  def this(name:String) = this(name, -1, new Date())
  def exist() = length != -1
}

object MongoFs {

  def list(qs:String) = {
    assert(!SwingUtilities.isEventDispatchThread)

    ConnectionPull.gridFs().getFileList(if(qs.isEmpty) new BasicDBObject() else JSON.parse(qs).asInstanceOf[DBObject]).toList.map{ f =>
      GfsFile(f.get("filename").asInstanceOf[String], f.get("length").asInstanceOf[Long], f.get("uploadDate").asInstanceOf[Date])
    }
  }

  def put(nm:String, dt: => InputStream){
    assert(!SwingUtilities.isEventDispatchThread)

    dt.autoClose {
      ConnectionPull.gridFs().createFile(_, nm).save()
    }
  }

  def load(nm:String, dt: => OutputStream){
    assert(!SwingUtilities.isEventDispatchThread)

    val f = ConnectionPull.gridFs().findOne(nm)
    if(f == null) throw new RuntimeException("not found "+f)
    f.getInputStream.autoClose{ in => dt.autoClose{ out =>
      val buffer = new Array[Byte](1024)
      var r = in.read(buffer)
      while(r != -1){
        out.write(buffer, 0, r)
        r = in.read(buffer)
      }
    }}
  }

  def delete(nm:String){
    assert(!SwingUtilities.isEventDispatchThread)

    ConnectionPull.gridFs().remove(nm)
  }

  def replace(nm:String, dt: => InputStream){
    assert(!SwingUtilities.isEventDispatchThread)

    val fs = ConnectionPull.gridFs()
    fs.remove(nm)

    dt.autoClose {
      fs.createFile(_, nm).save()
    }
  }

}
