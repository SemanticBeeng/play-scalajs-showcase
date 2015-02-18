package example

import scala.collection.Iterable
import scala.concurrent.Future
import scala.scalajs.js.annotation.{JSExportAll, JSExport}

//

import org.scalajs.dom.ext.{AjaxException, Ajax}

//

import shared.config.Routes
import shared.domain.todo._

//

import common.ExtAjax._
import example.TodoJS.Model
import upickle._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

/**
 *
 */
@JSExport
@JSExportAll
object TodoClient extends TaskManagement {

  def allScheduled(): Future[List[Task]] = {
    Future(Model.tasks())
  }

  /**
   *
   */
  override def scheduleNew(txt: String /*, done: Boolean*/): Future[ReturnVal[TaskId]] = {

    val json = s"""{"txt": "${txt}", "done": ${false}}"""
    Ajax.postAsJson(Routes.Todos.create, json).map { r =>

      read[ReturnVal[TaskId]](r.responseText)
    }.recover {
      // Trigger client side system exceptions
      case e: AjaxException => throw new TodoSystemException(e.xhr.responseText)
      case e1 => throw new TodoSystemException(e1.toString)
    }
  }

  /**
   *
   */
  override def redefine(taskId: TaskId, txt: String): Future[Iterable[TaskEvent]] = {

    val json = s"""{"taskId": $taskId, "txt": "$txt"}"""
    //task.id.map{ id =>
    Ajax.postAsJson(Routes.Todos.update(taskId.get), json).map { r =>

      read[Iterable[TaskEvent]](r.responseText)
    }.recover {
      // Trigger client side system exceptions
      case e: AjaxException => throw new TodoSystemException(e.xhr.responseText)
      case e1 => throw new TodoSystemException(e1.toString)
    }
  }

  /**
   *
   */
  override def complete(taskId: TaskId): Future[Iterable[TaskEvent]] = {

    Ajax.postAsJson(Routes.Todos.complete(taskId.get)).map { r =>
      //@todo implement
      read[Iterable[TaskEvent]](r.responseText)
    }.recover {
      // Trigger client side system exceptions
      case e: AjaxException => throw new TodoSystemException(e.xhr.responseText)
      case e1 => throw new TodoSystemException(e1.toString)
    }
  }

  /**
   *
   */
  override def cancel(id: TaskId): Future[ReturnVal[Boolean]] = {
    Ajax.delete(Routes.Todos.cancel(id.get)).map { r =>

      read[ReturnVal[Boolean]](r.responseText)
    }.recover {
      // Trigger client side system exceptions
      case e: AjaxException => throw new TodoSystemException(e.xhr.responseText)
      case e1 => throw new TodoSystemException(e1.toString)
    }
  }

  /**
   *
   */
  override def clearCompletedTasks(): Future[ReturnVal[Int]] = {
    Ajax.postAsForm(Routes.Todos.clear).map { r =>

      read[ReturnVal[Int]](r.responseText)
    }.recover {
      // Trigger client side system exceptions
      case e: AjaxException => throw new TodoSystemException(e.xhr.responseText)
      case e1 => throw new TodoSystemException(e1.toString)
    }
  }
}

