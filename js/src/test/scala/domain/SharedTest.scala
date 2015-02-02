package domain

import shared.domain.todo._
import shared.mock.TodoServerMock
import utest._

/**
 * @todo Test async support in uTest
 *       https://github.com/lihaoyi/utest/issues/33#issuecomment-65055113
 */

object SharedTest extends TestSuite {

  val taskOne = TaskId(1L)
  val taskTwo = TaskId(2L)

  def tests = TestSuite {

    /**
     * No need for explicit scoping like in Specs2 specs.
     * Each test gets a fresh copy of these variables
     */
    val taskPlan = new Plan
    val taskMgmt: TaskManagement = new TodoServerMock()

    "A plan can have a task" - {

      assert(taskPlan.size == 0)
      assert(taskPlan.size == 0)

      taskPlan.loadFromHistory(Seq(
        TaskScheduled(Task(taskOne, "Do this")),
        TaskRedefined(taskOne, "Do this other thing"),
        TaskCompleted(taskOne)))

      assert(taskPlan.size == 1)
      assert(taskPlan.countLeftToComplete == 0)

      taskMgmt.clearCompletedTasks

      assert(taskPlan.countLeftToComplete == 0)
    }

    "A plan can have two tasks" - {

      assert(taskPlan.size == 0)

      taskPlan.loadFromHistory(Seq(
        TaskScheduled(Task(taskOne, "Do this")),
        TaskRedefined(taskOne, "Do this other thing"),
        TaskScheduled(Task(taskTwo, "Do this honey")),
        TaskCompleted(taskOne)))

      assert(taskPlan.size == 2)

      taskMgmt.clearCompletedTasks

      assert(taskPlan.countLeftToComplete == 1)
    }
  }
}
