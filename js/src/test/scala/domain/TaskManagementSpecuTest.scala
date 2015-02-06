package domain

import shared.domain.TaskManagementModelProxy
import shared.domain.todo._
import utest._

import scala.concurrent.Future

/**
 * @todo Test async support in uTest
 *       https://github.com/lihaoyi/utest/issues/33#issuecomment-65055113
 */

object TaskManagementSpecuTest extends TestSuite /*with TaskManagementScope*/ {

  implicit val ec = ExecutionContext.RunNow

  def tests = TestSuite {

    /**
     * No need for explicit scoping like in Specs2 specs.
     * Each test gets a fresh copy of these variables
     */
    val $ = new TaskManagementModelProxy()

    "A plan can have a task" - {

      assert($.taskPlan.isEmpty)

      $.taskPlan.loadFromHistory(Seq(
        TaskScheduled(Task($.taskOne, "Do this")),
        TaskRedefined($.taskOne, "Do this other thing"),
        TaskCompleted($.taskOne)))

      assert($.taskPlan.size == 1)
      assert($.taskPlan.countLeftToComplete == 0)

      $.taskMgmt.clearCompletedTasks

      assert($.taskPlan.countLeftToComplete == 0)
    }

    "A plan can have two tasks" - {

      assert($.taskPlan.isEmpty)

      $.taskPlan.loadFromHistory(Seq(
        TaskScheduled(Task($.taskOne, "Do this")),
        TaskRedefined($.taskOne, "Do this other thing"),
        TaskScheduled(Task($.taskTwo, "Do this honey")),
        TaskCompleted($.taskOne)))

      assert($.taskPlan.size == 2)

      $.taskMgmt.clearCompletedTasks

      assert($.taskPlan.countLeftToComplete == 1)
    }

    "fff" - {
      Future {
        assert(true)
      }
    }

    "A plan can have two tasks" - {

      assert($.taskPlan.isEmpty)

      $.do_scheduleNew("Do this") andThen { case r =>

        val taskId = r.get.value
        $.do_complete(taskId)

      } andThen { case _ =>

        $.do_clearCompletedTasks
      }
    }

  }
}
