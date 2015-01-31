package showcase.tests

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import shared.domain.todo._
import shared.mock.TodoServerMock

//import scala.concurrent.ExecutionContext.Implicits.global

/**
 *
 */
@RunWith(classOf[JUnitRunner])
class BusinessSpec extends Specification {

  val taskPlan = new Plan
  val taskMgmt: TaskManagement = new TodoServerMock()

  "A plan" should {

    "accept one task and later complete it" in {

      taskPlan.loadFromHistory(Seq(
        TaskCreated(new Task(Some(1L), "Do this")),
        TaskRedefined(1L, "Do this other thing")))

      taskPlan.size should be_==(1)
      taskPlan.countLeftToComplete should be_==(1)
      taskPlan.markCommitted

      taskMgmt.complete(1L) foreach { history =>
        taskPlan.loadFromHistory(history.seq)
        //true

        taskPlan.countLeftToComplete should be_==(0)

        taskMgmt.clearCompletedTasks.foreach { history =>

          taskPlan.loadFromHistory(history.seq)

          taskPlan.countLeftToComplete should be_==(0)
          taskPlan.size should be_==(0)

        }
      }
      success
      //failure
      //      }.recover {
      //
      //        case _ => failure("Failed with ")
      //        failure("Unknown failure executing async call")
      //      }.onComplete { r =>
      //        failure
      //      }
    }

    "accept two tasks and complete them separately" in {
      val plan = new Plan
      plan.loadFromHistory(Seq(
        TaskCreated(new Task(Some(1L), "Do this")),
        TaskRedefined(1L, "Do this other thing")))

      plan.size should be_==(1)
      plan.countLeftToComplete should be_==(1)

      plan.loadFromHistory(Seq(
        TaskCreated(new Task(Some(2L), "Do this honey")),
        TaskCompleted(1L)))

      plan.countLeftToComplete should be_==(1)

      taskMgmt.clearCompletedTasks.foreach { history =>

        plan.loadFromHistory(history)

        plan.countLeftToComplete should be_==(1)
        plan.size should be_==(1)
      }
      success
      //        .recover {
      //
      //        case e => failure("Failed with " + e.getMessage)
      //        failure("Unknown failure executing async call")
      //      }
    }
  }
}
