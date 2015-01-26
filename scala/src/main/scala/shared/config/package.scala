package shared

import scala.concurrent.Future

/**
 *
 */
package object config {

  trait TodoIntf {

    def all: Future[List[Task]]
    def create(txt: String, done: Boolean) : Future[Either[Task, TodoBusinessException]]
    def update(task: Task): Future[Boolean]
    def delete(id: Long): Future[Boolean]
    def clearCompletedTasks : Future[Boolean]

  }

  trait TodoException {
    def message: String

    override def toString = message
  }

  case class TodoBusinessException(message:String) extends Exception(message) with TodoException
  case class TodoSystemException(message:String) extends RuntimeException(message) with TodoException

}
