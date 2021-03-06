package org.gfs.mongo

import java.io.{InputStream, OutputStream}
import java.util.Date
import javax.swing.SwingUtilities

import com.mongodb.gridfs.GridFS
import com.mongodb.util.JSON
import com.mongodb.{BasicDBObject, DBObject, MongoClient, WriteConcern}
import org.gfs.mongo.auto._

import scala.collection.JavaConversions.asScalaIterator
import org.gfs.Api

object ConnectionPull {
  val lock = new Object
  var pull : Option[MongoClient] = None

  var dbNm : Option[String] = None
  //var dbBucket : Option[String] = None

  def pool() = {
    assert(!SwingUtilities.isEventDispatchThread)

    lock.synchronized{ pull.get }
  }

  def apply() ={
    assert(!SwingUtilities.isEventDispatchThread)

    pool().getDB(dbNm.get)
  }

  def connect(host:String, port:Int, db:String) {
    assert(!SwingUtilities.isEventDispatchThread)

    lock.synchronized {
      pull = Some(new MongoClient(host, port))
      dbNm = if(db.isEmpty) None else Some(db)
      //dbBucket = if(bucket.isEmpty) None else Some(bucket)
    }
  }

  def gridFs(dbBucket: Option[String] /*= None*/) = lock.synchronized {
    assert(!SwingUtilities.isEventDispatchThread)

    val db = apply()
    db.setWriteConcern(WriteConcern.REPLICAS_SAFE)
    if (dbBucket.isEmpty) new GridFS(db) else new GridFS(db, dbBucket.get)
  }

  def colsNm() = apply().getCollectionNames

  def close(){
    lock.synchronized {
      pull.foreach(_.close())
    }
  }

}

object GfsFile{
  def apply(name:String) = new GfsFile(name)
}

case class GfsFile(name:String, length:Long, uploadDate:Date){
  def this(name:String) = this(name, -1, new Date())
  def exist() = length != -1
}

class MongoFs(fs:GridFS) {

  def list(qs:String) = {
    assert(!SwingUtilities.isEventDispatchThread)

    fs.getFileList(if(qs.isEmpty) new BasicDBObject() else JSON.parse(qs).asInstanceOf[DBObject]).toList.map{ f =>
      GfsFile(f.get("filename").asInstanceOf[String], f.get("length").asInstanceOf[Long], f.get("uploadDate").asInstanceOf[Date])
    }
  }

  def put(nm:String, dt: => InputStream){
    assert(!SwingUtilities.isEventDispatchThread)

    dt.autoClose { fs.createFile(_, nm).save() }

    Api.event("add", nm)
  }

  def load(nm:String, dt: => OutputStream){
    assert(!SwingUtilities.isEventDispatchThread)

    val f = fs.findOne(nm)
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

    fs.remove(nm)

    Api.event("remove", nm)
  }

  def replace(nm:String, dt: => InputStream){
    assert(!SwingUtilities.isEventDispatchThread)

    fs.remove(nm)
    dt.autoClose { fs.createFile(_, nm).save() }

    Api.event("replace", nm)
  }

}
