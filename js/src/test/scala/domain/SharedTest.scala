package domain

import shared.domain.todo.{Plan, Task, TaskCreated}
import utest._

/**
 *
 */

object SharedTest extends TestSuite {

  def tests = TestSuite {

    "A plan can have a task" - {
      val plan = new Plan
      plan.loadFromHistory(Seq(
        TaskCreated(new Task(Some(1L), "Do this", false))
      ))
      assert(plan.countLeftToComplete == 1)
    }
  }
}
