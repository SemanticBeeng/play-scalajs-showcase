package controllers

import models.TaskMemStore.InsufficientStorageException
import models.TaskModel
import shared.domain.todo._

import scala.collection.Iterable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
/**
 *
 */
object TodoServer extends TaskManagement {

  /**
   *
   */
  override def allScheduled(): Future[List[Task]] = {
    TaskModel.store.all
  }

  /**
   *
   */
  override def scheduleNew(txt: String/*, done: Boolean*/): Future[ReturnVal[TaskId]] = {

    TaskModel.store.create(txt, false).map { task =>
      null //ReturnVal(task.id) @todo implement
    }.recover {
      // @todo Reconsider this approach
      case e: InsufficientStorageException => return Future(ReturnVal(Right(new TaskBusinessException(e.getMessage))))
      case e: Throwable => throw new TodoSystemException(e.getMessage)
    }
  }

  /**
   *
   */
  override def redefine(taskId: TaskId, txt: String): Future[Iterable[TaskEvent]] = {
    //@todo implement TaskModel.store.update(task)
    Future(List.empty[TaskEvent].toIterable)
  }

  /**
   *
   */
  override def complete(taskId: TaskId): Future[Iterable[TaskEvent]] = {
    val task = null //@todo implement TaskModel.store.find(taskId)
    TaskModel.store.update(task)
    Future(List.empty[TaskEvent].toIterable)
  }

  /**
   *
   */
  override def cancel(id: TaskId): Future[ReturnVal[Boolean]] = {
    //@todo implement event history
    //Future(ReturnVal(Left(TaskModel.store.delete(id.get))))
    Future(ReturnVal(Left(true)))
  }

  /**
   *
   */
  override def clearCompletedTasks(): Future[ReturnVal[Int]] = {
    //@todo implement
    null
    //    TaskModel.store.clearCompletedTasks.map { r =>
    //      r > 0
    //    }
  }

}
