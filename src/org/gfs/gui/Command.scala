package org.gfs.gui

import java.util.concurrent.{Executor, Executors}
import javax.swing.SwingUtilities

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.language.implicitConversions

object Command{

  object implicits{
    implicit def asRun[F](f: => F) = new Runnable(){ def run() { f } }
  }

  val ecGui = ExecutionContext.fromExecutor(new Executor(){
    override def execute(command: Runnable){ SwingUtilities.invokeLater(command) }
  })

  val pull = Executors.newCachedThreadPool()
  val ec = ExecutionContext.fromExecutor(pull)

  def gui[T](f : => T) = new GuiCommand[T](f)
  def job[T](f : => T) = new JobCommand[T](f)
}

trait Command[T] extends Runnable{
  var next : Option[Task[T, _]] = None

  def map[V](t:Task[T, V]) = {
    next = Some(t)
    t
  }

  def toGui[V](nf : T => V) = map(new GuiTask(this, nf))
  def toJob[V](nf : T => V) = map(new JobTask(this, nf))

  def gui[V](nf : => V): Task[T, V] = toGui{a => nf}
  def job[V](nf : => V): Task[T, V] = toJob{a => nf}

  def execute(t: Task[T, _],b:T)
}

trait CommandGui[T] extends Command[T]{
  override def execute(t: Task[T, _], v:T){
    import org.gfs.gui.Command.implicits._
    t match {
      case g: GuiTask[T, _] => g.complete(v)
      case j: JobTask[T, _] => Command.ec.execute(j.complete(v))
    }
  }
}

trait CommandJob[T] extends Command[T]{
  override def execute(t: Task[T, _], v:T){
    import org.gfs.gui.Command.implicits._
    t match {
      case g: GuiTask[T, _] => Command.ecGui.execute(g.complete(v))
      case j: JobTask[T, _] => j.complete(v)
    }
  }
}

abstract class BaseCommand[T](f: => T, ec:ExecutionContextExecutor) extends Command[T]{
  override def run() {
    import org.gfs.gui.Command.implicits._
    ec.execute(asRun{
      val v = f
      next.foreach(execute(_, v))
    })
  }
}

class GuiCommand[T](f: => T) extends BaseCommand[T](f, Command.ecGui) with CommandGui[T]
class JobCommand[T](f: => T) extends BaseCommand[T](f, Command.ec) with CommandJob[T]

abstract class Task[A, B](c: Command[_], f: A => B) extends Command[B]{
  override def run(){ c.run() }

  def complete(a: A){
    val v = f(a)
    next.foreach(execute(_, v))
  }

}

class GuiTask[A, B](c: Command[_], f: A => B) extends Task[A, B](c, f) with CommandGui[B]
class JobTask[A, B](c: Command[_], f: A => B) extends Task[A, B](c, f) with CommandJob[B]
