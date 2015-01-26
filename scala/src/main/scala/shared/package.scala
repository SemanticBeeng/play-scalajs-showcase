import scala.concurrent.Future

/**
 *
 */
package object shared {

  /**
   * Shared business API between "*jvm" and "*js" sub projects.
   * This approach is designed to enable writing of business logic in a way that is transparent to the layer and to execute such business logic on any tier as appropriate. This is necessary because business logic cross cuts the layers : client, server, data access layer, UI, etc
   */
  trait TodoIntf {

    def all: Future[List[Task]]
    def create(txt: String, done: Boolean) : Future[Either[Task, TodoBusinessException]]
    def update(task: Task): Future[Boolean]
    def delete(id: Long): Future[Boolean]
    def clearCompletedTasks : Future[Boolean]

  }

  case class Task(id: Option[Long], txt: String, done: Boolean)

  trait TodoException {
    def message: String

    override def toString = message
  }

  case class TodoBusinessException(message:String) extends Exception(message) with TodoException
  case class TodoSystemException(message:String) extends RuntimeException(message) with TodoException

}
