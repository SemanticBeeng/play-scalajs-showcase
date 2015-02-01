package shared.domain

import shared.domain.immutabledomain.AggregateRoot

import scala.collection.{Iterable}
import scala.concurrent.Future

/**
 *
 */
package object todo {

  /**
   * Shared business API between "*jvm" and "*js" sub projects.
   * This approach is designed to enable writing of business logic in a way that is transparent to the layer and to execute such business logic on any tier as appropriate. This is necessary because business logic cross cuts the layers : client, server, data access layer, UI, etc
   */
  trait TaskManagement {

    def allScheduled: Future[List[Task]]

    def scheduleNew(txt: String, done: Boolean = false): Future[Either[Iterable[TaskEvent], TaskBusinessException]]

    def redefine(taskId: Long, txt: String): Future[Iterable[TaskEvent]]

    def complete(taskId: Long): Future[Iterable[TaskEvent]]

    def cancel(taskId: Long): Future[Boolean]

    def clearCompletedTasks: Future[Iterable[TaskEvent]]

  }

  case class ReturnVal[T] (v : T, events : Iterable[TaskEvent], e : Option[TaskBusinessException] = None)
  
  case class Task(id: Option[Long], var txt: String, var done: Boolean = false)

  sealed trait TaskEvent

  // Event protocol
  case class TaskScheduled(task: Task) extends TaskEvent

  case class TaskRedefined(taskId: Long, txt: String) extends TaskEvent

  case class TaskCompleted(taskId: Long) extends TaskEvent

  case class CompleteCleared() extends TaskEvent

  case class TaskCancelled(taskId: Long) extends TaskEvent

  /**
   * DDD Aggregate
   */
  class Plan extends AggregateRoot[TaskEvent] {

    var tasks = List.empty[Task]

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
    def findById(taskId: Long): Option[Task] = tasks.find(t => t.id.get == taskId)

    /**
     *
     */
    def newTask(task: Task) {

      record(TaskScheduled(task))
    }

    /**
     *
     */
    def markCompleted(taskId: Long) = {

      record(TaskCompleted(taskId))
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
          record(TaskCancelled(task.id.get))
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
          if (task.id.get == event.taskId) {
            task.txt = event.txt
          }
        }

      case event: CompleteCleared =>

        Nil

      case event: TaskCompleted =>

        tasks.foreach { task =>
          if (task.id.get == event.taskId) {
            task.done = true
          }
        }

      case event: TaskCancelled =>

        tasks = tasks.dropWhile(task => task.id.get == event.taskId)

    }
  }

  trait TodoException {
    def message: String

    override def toString = message
  }

  case class TaskBusinessException(message: String) extends Exception(message) with TodoException

  case class TodoSystemException(message: String) extends RuntimeException(message) with TodoException

}
