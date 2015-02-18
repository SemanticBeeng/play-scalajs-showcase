package shared.domain

import shared.domain.immutabledomain.AggregateRoot

import scala.collection.Iterable
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js.annotation.JSExport
import scala.util.Either

/**
 *
 */
package object todo {

  /**
   * Shared business API between "*jvm" and "*js" sub projects.
   * This approach is designed to enable writing of business logic in a way that is transparent to the layer and to execute such business logic on any tier as appropriate. This is necessary because business logic cross cuts the layers : client, server, data access layer, UI, etc
   */
  trait TaskManagement {

    def allScheduled()(implicit ec: ExecutionContext): Future[List[Task]]

    def scheduleNew(txt: String, done: Boolean = false)(implicit ec: ExecutionContext): Future[ReturnVal[TaskId]]

    def redefine(taskId: TaskId, txt: String)(implicit ec: ExecutionContext): Future[Iterable[TaskEvent]]

    def complete(taskId: TaskId)(implicit ec: ExecutionContext): Future[Iterable[TaskEvent]]

    def cancel(taskId: TaskId)(implicit ec: ExecutionContext): Future[ReturnVal[Boolean]]

    def clearCompletedTasks()(implicit ec: ExecutionContext): Future[ReturnVal[Int]]
  }

  /**
   * Helper class to wrap a {return value or business exception} and the history of applied events
   */
  case class ReturnVal[T](v: Either[T, TaskBusinessException], events: Iterable[TaskEvent] = Nil) {
    def value = v.left.get

    def ex = v.right.get
  }

  final case class TaskId(id: Long) {
    def get = id // make it look like an Option
  }

  object TaskId {

    import scala.language.implicitConversions

    val nullId = Long.MinValue //@todo a hack that will not last..

    implicit def fromOption(xo: Option[Long]): TaskId = if (!xo.isEmpty) TaskId(xo.get) else TaskId(nullId)

    implicit def toOption(taskId: TaskId): Option[Long] = if (taskId.id != nullId) Some(taskId.id) else None

  }

  /**
   * Domain object
   *
   * Note: In Idea must add the "SBT: org.scala-js:scalajs-library_2.11:0.6.0" dependency manually for the "sahred"
   * module in oder for @JsExport to be recognised. Otheriwse the build works
   */
  @JSExport
  case class Task(id: TaskId, var txt: String, var done: Boolean = false)

  sealed trait TaskEvent

  // Event protocol
  case class TaskScheduled(task: Task) extends TaskEvent

  case class TaskRedefined(taskId: TaskId, txt: String) extends TaskEvent

  case class TaskCompleted(taskId: TaskId) extends TaskEvent

  case class CompleteCleared() extends TaskEvent

  case class TaskCancelled(taskId: TaskId) extends TaskEvent

  /**
   * DDD Aggregate
   */
  class Plan extends AggregateRoot[TaskEvent] {

    var tasks = List.empty[Task]

    /**
     *
     */
    def isEmpty: Boolean = size == 0 && countCompleted == 0 && countLeftToComplete == 0

    /**
     *
     */
    def size: Int = tasks.size

    /**
     *
     */
    def countLeftToComplete: Int = tasks.count(t => !t.done)

    /**
     *
     */
    def countCompleted: Int = tasks.count(t => t.done)

    /**
     *
     */
    def findById(taskId: TaskId): Option[Task] = tasks.find(t => t.id == taskId)

    /**
     *
     */
    def newTask(task: Task) {

      record(TaskScheduled(task))
    }

    /**
     *
     */
    def markCompleted(taskId: TaskId) = {

      record(TaskCompleted(taskId))
    }

    /**
     *
     */
    def cancel(taskId: TaskId): Boolean = {
      val sizeBefore = size
      //@todo I could do this in applyEvent but did not want to mix calls to record in applyEvent
      record(TaskCancelled(taskId))
      sizeBefore == size + 1
    }

    /**
     *
     */
    def clearCompletedTasks: Int = {

      val sizeBefore = size
      record(CompleteCleared())
      //@todo I could do this in applyEvent but did not want to mix calls to record in applyEvent
      tasks.foreach { task =>
        if (task.done) {
          record(TaskCancelled(task.id))
        }
      }
      sizeBefore - size
    }

    /**
     *
     */
    protected def applyEvent = {

      case event: TaskScheduled =>
        tasks = event.task +: tasks

      case event: TaskRedefined =>

        tasks.foreach { task =>
          if (task.id == event.taskId) {
            task.txt = event.txt
          }
        }

      case event: CompleteCleared =>

        Nil

      case event: TaskCompleted =>

        tasks.foreach { task =>
          if (task.id == event.taskId) {
            task.done = true
          }
        }

      case event: TaskCancelled =>

        tasks = tasks.dropWhile(task => task.id == event.taskId)

    }
  }

  trait TodoException {
    def message: String

    override def toString = message
  }

  case class TaskBusinessException(message: String) extends Exception(message) with TodoException

  case class TodoSystemException(message: String) extends RuntimeException(message) with TodoException

}
