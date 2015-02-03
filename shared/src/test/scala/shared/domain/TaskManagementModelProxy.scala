package shared.domain

import shared.domain.todo._
import shared.mock.TodoServerMock

import scala.concurrent.{ExecutionContext, Future}

/**
 *
 */
trait TaskManagementScope {

  val taskOne = TaskId(1L)
  val taskTwo = TaskId(2L)

  val taskPlan = new Plan
  val taskMgmt: TaskManagement = new TodoServerMock()
}

/**
 *
 */
class TaskManagementModelProxy extends TaskManagementScope {

  /**
   *
   */
  def do_scheduleNew(txt: String)(implicit ec: ExecutionContext): Future[ReturnVal[TaskId]] = {

    val planSizeBefore = taskPlan.size
    val countLeftToCompleteBefore = taskPlan.countLeftToComplete

    val future: Future[ReturnVal[TaskId]] = taskMgmt.scheduleNew(txt)
    future map { returnVal =>

      assert(returnVal.v.isLeft)

      taskPlan.loadFromHistory(returnVal.events)

      assert(taskPlan.size == planSizeBefore + 1)
      assert(taskPlan.countLeftToComplete == (countLeftToCompleteBefore + 1),
        "Unexpected countLeftToComplete " + taskPlan.countLeftToComplete + " vs " + (countLeftToCompleteBefore + 1))
      taskPlan.markCommitted

      val lastTaskUsed = taskPlan.findById(returnVal.value)
      assert(lastTaskUsed.get.txt.equals(txt))

    }
    future
  }

  /**
   *
   */
  def do_redefine(taskId: TaskId, newTxt: String)(implicit ec: ExecutionContext): Future[Iterable[TaskEvent]] = {

    val planSizeBefore = taskPlan.size
    val countLeftToCompleteBefore = taskPlan.countLeftToComplete

    val future: Future[Iterable[TaskEvent]] = taskMgmt.redefine(taskId, newTxt)

    future map { events =>

      taskPlan.loadFromHistory(events)

      assert(taskPlan.size == planSizeBefore)
      assert(taskPlan.countLeftToComplete == countLeftToCompleteBefore)

      assert(taskPlan.tasks.head.txt.equals("Do this other thing"))
    }
    future
  }

  /**
   *
   */
  def do_complete(taskId: TaskId)(implicit ec: ExecutionContext): Future[Iterable[TaskEvent]] = {

    val planSizeBefore = taskPlan.size
    val countLeftToCompleteBefore = taskPlan.countLeftToComplete

    val future: Future[Iterable[TaskEvent]] = taskMgmt.complete(taskId)

    future andThen { case r =>

      val events = r.get
      taskPlan.loadFromHistory(events)

      assert(taskPlan.size == planSizeBefore)
      val clearedCount = 1
      assert(taskPlan.countLeftToComplete == (countLeftToCompleteBefore - clearedCount),
        "Unexpected countLeftToComplete " + taskPlan.countLeftToComplete + " vs " + (countLeftToCompleteBefore - clearedCount))

    }

    future
  }

  /**
   *
   */
  def do_clearCompletedTasks()(implicit ec: ExecutionContext): Future[ReturnVal[Int]] = {

    val planSizeBefore = taskPlan.size

    val future: Future[ReturnVal[Int]] = taskMgmt.clearCompletedTasks
    future andThen { case r =>

      val returnVal: ReturnVal[Int] = r.get
      taskPlan.loadFromHistory(returnVal.events)

      println("Cleared " + returnVal.value + " tasks")
      assert(taskPlan.size == (planSizeBefore - returnVal.value))
      assert(taskPlan.countCompleted == 0)

    }

    future
  }
}
