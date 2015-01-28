package shared.domain

import shared.domain.immutabledomain.AggregateRoot

import scala.collection.mutable
import scala.concurrent.Future

/**
 *
 */
package object todo {

  /**
   * Shared business API between "*jvm" and "*js" sub projects.
   * This approach is designed to enable writing of business logic in a way that is transparent to the layer and to execute such business logic on any tier as appropriate. This is necessary because business logic cross cuts the layers : client, server, data access layer, UI, etc
   */
  trait TodoIntf {

    def all: Future[List[Task]]

    def create(txt: String, done: Boolean): Future[Either[Task, TodoBusinessException]]

    def update(task: Task): Future[Boolean]

    def delete(id: Long): Future[Boolean]

    def clearCompletedTasks: Future[Boolean]

  }

  case class Task(id: Option[Long], var txt: String, var done: Boolean)

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

    var tasks:mutable.MutableList[Task] = mutable.MutableList.empty[Task]

    def newTask(task: Task) {

      record(TaskCreated(task))
    }

    def countLeftToComplete : Int = tasks.count( t => !t.done)

    protected def applyEvent = {

      case event: TaskCreated =>
        tasks = event.task +: tasks

      case event: TaskRedefined =>

        val pos = tasks.indexWhere(t => t.id.get == event.taskId)
        val updatedTask: Option[Task] = tasks.get(pos)
        updatedTask.get.txt = event.txt
        //tasks = tasks.updated(pos, updatedTask)

      case event: TaskCompleted =>

        val pos = tasks.indexWhere(t => t.id.get == event.taskId)
        val updatedTask: Option[Task] = tasks.get(pos)
        updatedTask.get.done = true
      //tasks = tasks.updated(pos, updatedTask)

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
