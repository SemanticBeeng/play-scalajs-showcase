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

    def scheduleNew(txt: String, done: Boolean): Future[Either[Iterable[TaskEvent], TodoBusinessException]]

    def redefine(task: Task): Future[Boolean]

    def complete(taskId: Long): Future[Iterable[TaskEvent]]

    def cancel(taskId: Long): Future[Boolean]

    def clearCompletedTasks: Future[Iterable[TaskEvent]]

  }

  case class Task(id: Option[Long], var txt: String, var done: Boolean = false)

  sealed trait TaskEvent {
    //val taskId: Long
  }

  // Event protocol
  case class TaskCreated(task: Task) extends TaskEvent

  case class TaskRedefined(taskId: Long, txt: String) extends TaskEvent

  case class TaskCompleted(taskId: Long) extends TaskEvent

  case class TaskDeleted(taskId: Long) extends TaskEvent

  /**
   * DDD Aggregate
   */
  class Plan extends AggregateRoot[TaskEvent] {
    var tasks = List.empty[Task]

    def newTask(task: Task) {

      record(TaskCreated(task))
    }

    /**
     *
     */
    def size: Int = tasks.size

    /**
     *
     */
    def countLeftToComplete = tasks.count(t => !t.done)

    def markCompleted(taskId: Long) = {
      //      tasks.foreach{ t =>
      //        if(t.id.get == taskId) {
      //          t.done = true
      //        }
      //      }
      record(TaskCompleted(taskId))
    }

    /**
     *
     */
    def clearCompletedTasks: Int = {

      tasks.foldLeft[Int](0) { case (c, task) =>
        if (task.done) {
          record(TaskDeleted(task.id.get))
          c + 1
        }
        else c
      }
    }

    /**
     *
     */
    protected def applyEvent = {

      case event: TaskCreated =>
        tasks = event.task +: tasks

      case event: TaskRedefined =>

        tasks.foreach { t =>
          if (t.id.get == event.taskId) {
            t.txt = event.txt
          }
        }
      //        val pos = tasks.indexWhere(t => t.id.get == event.taskId)
      //        val updatedTask: Option[Task] = tasks.get(pos)
      //        updatedTask.get.txt = event.txt
      //      //tasks = tasks.updated(pos, updatedTask)

      case event: TaskCompleted =>

        tasks.foreach { t =>
          if (t.id.get == event.taskId) {
            t.done = true
          }
        }
      //        val pos = tasks.indexWhere(t => t.id.get == event.taskId)
      //        val updatedTask: Option[Task] = tasks.get(pos)
      //        updatedTask.get.done = true
      //      //tasks = tasks.updated(pos, updatedTask)

      case event: TaskDeleted =>

        val pos = tasks.indexWhere(t => t.id.get == event.taskId)
        tasks.drop(pos)

    }
  }

  trait TodoException {
    def message: String

    override def toString = message
  }

  case class TodoBusinessException(message: String) extends Exception(message) with TodoException

  case class TodoSystemException(message: String) extends RuntimeException(message) with TodoException

}
