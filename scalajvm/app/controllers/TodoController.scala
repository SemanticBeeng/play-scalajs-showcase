package controllers

import models.TaskMemStore.InsufficientStorageException
import models.TaskModel
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc._
import shared.Task
import shared.config.{TodoBusinessException, TodoIntf, TodoSystemException}
import upickle._

import scala.concurrent.Future

/**
 *
 */
object TodoServer extends TodoIntf {

  /**
   *
   */
  override def all: Future[List[Task]] = {
    TaskModel.store.all
  }

  /**
   *
   */
  override def create(txt: String, done: Boolean): Future[Either[Task, TodoBusinessException]] = {

    TaskModel.store.create(txt, done).map { task =>
      Left(task)
    }.recover {
      // @todo Reconsider this approach
      case e: InsufficientStorageException => return Future(Right(new TodoBusinessException(e.getMessage)))
      case e: Throwable => throw new TodoSystemException(e.getMessage)
    }
  }

  /**
   *
   */
  override def update(task: Task): Future[Boolean] = {
    TaskModel.store.update(task)
  }

  /**
   *
   */
  override def delete(id: Long): Future[Boolean] = {
    TaskModel.store.delete(id)
  }

  /**
   *
   */
  override def clearCompletedTasks: Future[Boolean] = {
    TaskModel.store.clearCompletedTasks.map { r =>
      r > 0
    }
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
    TodoServer.all.map { r =>
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
      TodoServer.create(txt, done).map { r =>
        Ok(write(r))
      }
    executeRequest(fn)
  }

  /**
   *
   */
  def update(id: Long) = Action.async(parse.json) { implicit request =>
    val fn = (txt: String, done: Boolean) =>

      // @nick Delegate to implementation of shared API
      TodoServer.update(Task(Some(id), txt, done)).map { r =>
        if (r)
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
    TodoServer.delete(id).map { r =>
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
