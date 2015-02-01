package domain

import shared.domain.todo._
import shared.mock.TodoServerMock
import utest._

/**
 *
 */

object SharedTest extends TestSuite {

  val taskOne: Long = 1L
  val taskTwo: Long = 2L

  def tests = TestSuite {

    val taskPlan = new Plan
    val todoApi:TaskManagement = new TodoServerMock()

    "A plan can have a task" - {
      
      taskPlan.loadFromHistory(Seq(
        TaskScheduled(new Task(Some(taskOne), "Do this")),
        TaskRedefined(taskOne, "Do this other thing"),
        TaskCompleted(taskOne)))

      assert(taskPlan.size == 1)
      assert(taskPlan.countLeftToComplete == 0)

      todoApi.clearCompletedTasks

      assert(taskPlan.countLeftToComplete == 0)
    }

    "A plan can have two tasks" - {

      taskPlan.loadFromHistory(Seq(
        TaskScheduled(new Task(Some(taskOne), "Do this")),
        TaskRedefined(taskOne, "Do this other thing"),
        TaskScheduled(new Task(Some(taskTwo), "Do this honey")),
        TaskCompleted(taskOne)))

      assert(taskPlan.size == 2)

      todoApi.clearCompletedTasks

      assert(taskPlan.countLeftToComplete == 1)
    }
  }
}
