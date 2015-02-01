package controllers

import models.TaskMemStore.InsufficientStorageException
import models.TaskModel
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc._
import shared.domain.todo._
import upickle._

import scala.collection.Iterable
import scala.concurrent.Future

/**
 *
 */
object TodoServer extends TaskManagement {

  /**
   *
   */
  override def allScheduled: Future[List[Task]] = {
    TaskModel.store.all
  }

  /**
   *
   */
  override def scheduleNew(txt: String, done: Boolean): Future[Either[Iterable[TaskEvent], TaskBusinessException]] = {

    TaskModel.store.create(txt, done).map { task =>
      null //Left(task) @todo
    }.recover {
      // @todo Reconsider this approach
      case e: InsufficientStorageException => return Future(Right(new TaskBusinessException(e.getMessage)))
      case e: Throwable => throw new TodoSystemException(e.getMessage)
    }
  }

  /**
   *
   */
  override def redefine(taskId: Long, txt: String): Future[Iterable[TaskEvent]] = {
    //@todo implement TaskModel.store.update(task)
    Future(List.empty[TaskEvent].toIterable)
  }

  /**
   *
   */
  override def complete(taskId: Long): Future[Iterable[TaskEvent]] = {
    val task = null //@todo implement TaskModel.store.find(taskId)
    TaskModel.store.update(task)
    Future(List.empty[TaskEvent].toIterable)
  }

  /**
   *
   */
  override def cancel(id: Long): Future[Boolean] = {
    TaskModel.store.delete(id)
  }

  /**
   *
   */
  override def clearCompletedTasks: Future[Iterable[TaskEvent]] = {
    //@todo implement
    null
    //    TaskModel.store.clearCompletedTasks.map { r =>
    //      r > 0
    //    }
  }
}

/**
 *
 */
object TodoController extends Controller {

  implicit val jsonReader = (
    (__ \ 'txt).read[String](minLength[String](2)) and
      (__ \ 'done).read[Boolean]
    ).tupled

  def index = Action { implicit request =>
    Ok(views.html.todo("TODO"))
  }

  /**
   *
   */
  def all = Action.async { implicit request =>
    // @nick Delegate to implementation of shared API
    TodoServer.allScheduled.map { r =>
      Ok(write(r))
    }.recover {
      case err => InternalServerError(err.getMessage)
    }
  }

  /**
   *
   */
  def create = Action.async(parse.json) { implicit request =>
    val fn = (txt: String, done: Boolean) =>

      // @nick Delegate to implementation of shared API
      TodoServer.scheduleNew(txt, done).map { r =>
        Ok(write(r))
      }
    executeRequest(fn)
  }

  /**
   *
   */
  def update(id: Long) = Action.async(parse.json) { implicit request =>
    val fn = (txt: String, done: Boolean) =>

      // @todo: implement (remove CRUd API)
      TodoServer.redefine(id, txt).map { r =>
        if (r != null)
          Ok(write(r))
        else
          BadRequest
      }.recover {
        case e => InternalServerError(e)
      }
    executeRequest(fn)
  }

  /**
   *
   */
  def delete(id: Long) = Action.async { implicit request =>

    // @nick Delegate to implementation of shared API
    TodoServer.cancel(id).map { r =>
      if (r)
        Ok(write(r))
      else
        BadRequest
    }.recover {
      case e => InternalServerError(e)
    }
  }

  /**
   *
   */
  def clear = Action.async { implicit request =>

    // @nick Delegate to implementation of shared API
    TodoServer.clearCompletedTasks.map { r =>
      Ok(write(r))
    }.recover {
      case e => InternalServerError(e)
    }
  }

  def executeRequest(fn: (String, Boolean) => Future[Result])
                    (implicit request: Request[JsValue]) = {
    request.body.validate[(String, Boolean)].map {
      case (txt, done) => {
        fn(txt, done)
      }
    }.recoverTotal {
      e => Future(BadRequest(e))
    }
  }

}
