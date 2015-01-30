package showcase.tests

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import shared.domain.todo._

//import scala.concurrent.ExecutionContext.Implicits.global

/**
 *
 */
@RunWith(classOf[JUnitRunner])
class BusinessSpec extends Specification {

  val todoApi:TodoIntf = null

  "A plan" should {

    "accept one task and later complete it" in {
      val plan = new Plan
      plan.loadFromHistory(Seq(
        TaskCreated(new Task(Some(1L), "Do this")),
        TaskRedefined(1L, "Do this other thing")))

      plan.size should be_==(1)
      plan.countLeftToComplete should be_==(1)
      plan.markCommitted

      plan.loadFromHistory(Seq(
        TaskCompleted(1L)))

      plan.countLeftToComplete should be_==(0)
      //todoApi.clearCompletedTasks

      plan.countLeftToComplete should be_==(0)
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
      //todoApi.clearCompletedTasks

      plan.countLeftToComplete should be_==(1)
    }
  }
}
