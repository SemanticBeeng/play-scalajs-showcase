package domain

import shared.domain.todo._
import shared.mock.TodoServerMock
import utest._

/**
 *
 */

object SharedTest extends TestSuite {

  def tests = TestSuite {

    val taskPlan = new Plan
    val todoApi:TaskManagement = new TodoServerMock()

    "A plan can have a task" - {
      
      taskPlan.loadFromHistory(Seq(
        TaskScheduled(new Task(Some(1L), "Do this")),
        TaskRedefined(1L, "Do this other thing"),
        TaskCompleted(1L)))

      assert(taskPlan.size == 1)
      assert(taskPlan.countLeftToComplete == 0)

      todoApi.clearCompletedTasks

      assert(taskPlan.countLeftToComplete == 0)
    }

    "A plan can have two tasks" - {
      val plan = new Plan
      taskPlan.loadFromHistory(Seq(
        TaskScheduled(new Task(Some(1L), "Do this")),
        TaskRedefined(1L, "Do this other thing"),
        TaskCompleted(2L),
        TaskScheduled(new Task(Some(2L), "Do this honey"))))

      assert(taskPlan.size == 2)

      todoApi.clearCompletedTasks

      assert(taskPlan.countLeftToComplete == 1)
    }
  }


}
