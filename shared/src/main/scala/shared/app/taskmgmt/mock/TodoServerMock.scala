package shared.app.taskmgmt.mock

import shared.domain.todo._

import scala.collection.Iterable
import scala.concurrent.{ExecutionContext, Future}

//import scala.concurrent.ExecutionContext.Implicits.global

/**
 *
 */
class TodoServerMock() extends TaskManagement {

  var plan = new Plan
  var nextId = 1L

  /**
   *
   */
  override def allScheduled()(implicit ec: ExecutionContext): Future[List[Task]] = Future {
    plan.tasks.toList
  }

  /**
   *
   */
  override def scheduleNew(txt: String/*, done: Boolean*/)(implicit ec: ExecutionContext): Future[ReturnVal[TaskId]] = Future {

    val task: Task = Task(TaskId(nextId), txt, false)
    plan.record(TaskScheduled(task))
    val history = plan.uncommittedEvents
    plan.markCommitted
    nextId = nextId + 1
    ReturnVal(Left(task.id), history)
  }

  /**
   *
   */
  override def redefine(taskId: TaskId, txt: String)(implicit ec: ExecutionContext): Future[Iterable[TaskEvent]] = Future {

    plan.record(TaskRedefined(taskId, txt))
    val history = plan.uncommittedEvents
    plan.markCommitted
    history
  }

  /**
   *
   */
  override def complete(taskId: TaskId)(implicit ec: ExecutionContext): Future[Iterable[TaskEvent]] = Future {

    plan.markCompleted(taskId)
    val history = plan.uncommittedEvents
    plan.markCommitted
    history
  }

  /**
   *
   */
  override def clearCompletedTasks()(implicit ec: ExecutionContext): Future[ReturnVal[Int]] = Future {

    val completedTasks: Int = plan.clearCompletedTasks
    val history = plan.uncommittedEvents
    plan.markCommitted
    ReturnVal(Left(completedTasks), history)
  }

  /**
   *
   */
  override def cancel(taskId: TaskId)(implicit ec: ExecutionContext): Future[ReturnVal[Boolean]] = Future {

    val cancelled: Boolean = plan.cancel(taskId)
    val history = plan.uncommittedEvents
    plan.markCommitted
    ReturnVal(Left(cancelled), history)
  }

}
