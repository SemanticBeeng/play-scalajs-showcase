package shared.mock

import shared.domain.todo._

import scala.concurrent.Future
//import scala.concurrent.ExecutionContext.Implicits.global
import utest.ExecutionContext.RunNow

/**
 *
 */
class TodoServerMock extends TodoIntf {

  val plan = new Plan
  var nextId = 1L

  override def all: Future[List[Task]] = Future {
    plan.tasks.toList
  }

  override def update(task: Task): Future[Boolean] = Future {
    true
  }

  override def clearCompletedTasks: Future[Boolean] = Future {

    true
  }

  override def delete(id: Long): Future[Boolean] = Future {
    true
  }

  override def create(txt: String, done: Boolean): Future[Either[Iterable[TaskEvent], TodoBusinessException]] = Future {
    plan.record(TaskCreated(new Task(Option(nextId), txt, done)))
    val history = plan.uncommittedEvents
    plan.markCommitted
    nextId = nextId + 1
    Left(history)
  }
}
