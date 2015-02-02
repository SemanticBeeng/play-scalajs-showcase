package shared.mock

import shared.domain.todo._

import scala.collection.Iterable
import scala.concurrent.Future

//import scala.concurrent.ExecutionContext.Implicits.global

import utest.ExecutionContext.RunNow

/**
 *
 */
class TodoServerMock() extends TaskManagement {

  var plan = new Plan
  var nextId = 1L

  /**
   *
   */
  override def allScheduled: Future[List[Task]] = Future {
    plan.tasks.toList
  }

  /**
   *
   */
  override def scheduleNew(txt: String, done: Boolean): Future[ReturnVal[TaskId]] = Future {

    val task: Task = Task(TaskId(nextId), txt, done)
    plan.record(TaskScheduled(task))
    val history = plan.uncommittedEvents
    plan.markCommitted
    nextId = nextId + 1
    ReturnVal(Left(task.id), history)
  }

  /**
   *
   */
  override def redefine(taskId: TaskId, txt: String): Future[Iterable[TaskEvent]] = Future {

    plan.record(TaskRedefined(taskId, txt))
    val history = plan.uncommittedEvents
    plan.markCommitted
    history
  }

  /**
   *
   */
  override def complete(taskId: TaskId): Future[Iterable[TaskEvent]] = Future {

    plan.markCompleted(taskId)
    val history = plan.uncommittedEvents
    plan.markCommitted
    history
  }

  /**
   *
   */
  override def clearCompletedTasks: Future[ReturnVal[Int]] = Future {

    val completedTasks: Int = plan.clearCompletedTasks
    val history = plan.uncommittedEvents
    plan.markCommitted
    ReturnVal(Left(completedTasks), history)
  }

  /**
   *
   */
  override def cancel(id: TaskId): Future[Boolean] = Future {
    true
  }

}
