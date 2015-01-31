package shared.mock

import shared.domain.todo._

import scala.collection.Iterable
import scala.concurrent.Future

//import scala.concurrent.ExecutionContext.Implicits.global

import utest.ExecutionContext.RunNow

/**
 *
 */
class TodoServerMock extends TaskManagement {

  var plan = new Plan
  var nextId = 1L

  override def allScheduled: Future[List[Task]] = Future {
    plan.tasks.toList
  }

  override def redefine(task: Task): Future[Boolean] = Future {
    true
  }

  override def complete(taskId: Long): Future[Iterable[TaskEvent]] = Future{
    plan.markCompleted(taskId)
    val history = plan.uncommittedEvents
    plan.markCommitted
    history
  }

  override def clearCompletedTasks: Future[Iterable[TaskEvent]] = Future {

    plan.clearCompletedTasks
    val history = plan.uncommittedEvents
    plan.markCommitted
    history
  }

  override def cancel(id: Long): Future[Boolean] = Future {
    true
  }

  override def scheduleNew(txt: String, done: Boolean): Future[Either[Iterable[TaskEvent], TodoBusinessException]] = Future {
    plan.record(TaskCreated(new Task(Option(nextId), txt, done)))
    val history = plan.uncommittedEvents
    plan.markCommitted
    nextId = nextId + 1
    Left(history)
  }
}
