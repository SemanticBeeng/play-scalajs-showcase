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

  val taskOne: Long = 1L

  val taskPlan = new Plan
  val taskMgmt: TaskManagement = new TodoServerMock()

  "I should be able to" should {

    /**
     *
     */
    "schedule one task, redefine and complete it" in {

      taskPlan.loadFromHistory(Seq(
        TaskScheduled(new Task(Some(taskOne), "Do this")),
        TaskRedefined(taskOne, "Do this other thing")))

      taskPlan.size should be_==(1)
      taskPlan.countLeftToComplete should be_==(1)

      taskPlan.loadFromHistory(Seq(
        TaskCompleted(taskOne)))

      taskPlan.size should be_==(1)
      taskPlan.countLeftToComplete should be_==(0)

      taskPlan.clearCompletedTasks should be_==(1)

      taskPlan.size should be_==(0)
      taskPlan.countLeftToComplete should be_==(0)

      taskPlan.clearCompletedTasks should be_==(0)
    }

    /**
     *
     */
    "schedule one task and cancel it" in {

      taskPlan.loadFromHistory(Seq(
        TaskScheduled(new Task(Some(taskOne), "Do this"))))

      taskPlan.size should be_==(1)
      taskPlan.countLeftToComplete should be_==(1)

      taskPlan.loadFromHistory(Seq(
        TaskCancelled(taskOne)))

      taskPlan.size should be_==(0)
      taskPlan.countLeftToComplete should be_==(0)

      taskPlan.clearCompletedTasks should be_==(0)

    }

    /**
     *
     */
    "schedule one task and complete it remotely" in {

      val scheduleNew = taskMgmt.scheduleNew("Do this")

      scheduleNew.andThen { case r =>

        val eventsOrException: Either[Iterable[TaskEvent], TodoBusinessException] = r.get
        eventsOrException.isLeft should beTrue

        taskPlan.loadFromHistory(eventsOrException.left.get)

        taskPlan.size should be_==(1)
        taskPlan.countLeftToComplete should be_==(1)
        taskPlan.markCommitted

      } andThen { case _ =>

        taskMgmt.complete(taskOne) andThen { case r =>

          val history = r.get
          taskPlan.loadFromHistory(history)

          taskPlan.countLeftToComplete should be_==(0)

        } andThen { case _ =>

          taskMgmt.clearCompletedTasks andThen { case r =>

            val events: Iterable[TaskEvent] = r.get
            taskPlan.loadFromHistory(events)

            taskPlan.countLeftToComplete should be_==(0)
            taskPlan.size should be_==(0)
          }
        }
      }

      //      scheduleNew.onSuccess { case r =>
      //          r.isLeft should beTrue
      //
      //          taskPlan.loadFromHistory(r.left.get)
      //
      //          taskPlan.size should be_==(1)
      //          taskPlan.countLeftToComplete should be_==(1)
      //          taskPlan.markCommitted
      //
      //          success
      //      }

      scheduleNew.onFailure {
        case t => println("An error has occurred: " + t.getMessage)

          failure("An error has occurred: " + t.getMessage)
      }
      success
    }

    //    "schedule two tasks and complete them separately" in {
    //      val plan = new Plan
    //      plan.loadFromHistory(Seq(
    //        TaskScheduled(new Task(Some(taskOne), "Do this")),
    //        TaskRedefined(taskOne, "Do this other thing")))
    //
    //      plan.size should be_==(1)
    //      plan.countLeftToComplete should be_==(1)
    //
    //      plan.loadFromHistory(Seq(
    //        TaskScheduled(new Task(Some(2L), "Do this honey")),
    //        TaskCompleted(taskOne)))
    //
    //      plan.countLeftToComplete should be_==(1)
    //
    //      taskMgmt.clearCompletedTasks.foreach { history =>
    //
    //        plan.loadFromHistory(history)
    //
    //        plan.countLeftToComplete should be_==(1)
    //        plan.size should be_==(1)
    //      }
    //      success
    //    }
  }
}
