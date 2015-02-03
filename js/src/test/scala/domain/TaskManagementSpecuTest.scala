package domain

import shared.domain.TaskManagementModelProxy
import shared.domain.todo._
import utest._

/**
 * @todo Test async support in uTest
 *       https://github.com/lihaoyi/utest/issues/33#issuecomment-65055113
 */

object TaskManagementSpecuTest extends TestSuite /*with TaskManagementScope*/ {

  implicit val ec = ExecutionContext.RunNow

  def tests = TestSuite  {

    /**
     * No need for explicit scoping like in Specs2 specs.
     * Each test gets a fresh copy of these variables
     */
//    val scope.taskPlan = new Plan
//    val taskMgmt: TaskManagement = new TodoServerMock()
    val scope = new TaskManagementModelProxy()

    "A plan can have a task" - {

      assert(scope.taskPlan.isEmpty)

      scope.taskPlan.loadFromHistory(Seq(
        TaskScheduled(Task(scope.taskOne, "Do this")),
        TaskRedefined(scope.taskOne, "Do this other thing"),
        TaskCompleted(scope.taskOne)))

      assert(scope.taskPlan.size == 1)
      assert(scope.taskPlan.countLeftToComplete == 0)

      scope.taskMgmt.clearCompletedTasks

      assert(scope.taskPlan.countLeftToComplete == 0)
    }

    "A plan can have two tasks" - {

      assert(scope.taskPlan.isEmpty)

      scope.taskPlan.loadFromHistory(Seq(
        TaskScheduled(Task(scope.taskOne, "Do this")),
        TaskRedefined(scope.taskOne, "Do this other thing"),
        TaskScheduled(Task(scope.taskTwo, "Do this honey")),
        TaskCompleted(scope.taskOne)))

      assert(scope.taskPlan.size == 2)

      scope.taskMgmt.clearCompletedTasks

      assert(scope.taskPlan.countLeftToComplete == 1)
    }

    "A plan can have two tasks" - {

      assert(scope.taskPlan.isEmpty)

      scope.do_scheduleNew("Do this") andThen { case r =>

        val taskId = r.get.value
        scope.do_complete(taskId)

      } andThen { case _ =>

        scope.do_clearCompletedTasks
      }
    }

  }
}
