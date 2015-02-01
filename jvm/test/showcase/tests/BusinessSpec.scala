package showcase.tests

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import org.specs2.runner.JUnitRunner
import shared.domain.todo._
import shared.mock.TodoServerMock

//import scala.concurrent.ExecutionContext.Implicits.global

/**
 *
 */
@RunWith(classOf[JUnitRunner])
class BusinessSpec extends Specification {

  trait PlanScope extends Scope {
    val taskOne: Long = 1L

    val taskPlan = new Plan
    val taskMgmt: TaskManagement = new TodoServerMock()
  }

  "I should be able to" should {

    /**
     *
     */
    "schedule one task, redefine and complete it" in new PlanScope {

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
    "schedule one task and cancel it" in new PlanScope {

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
    "schedule one task and complete it remotely" in new PlanScope {

      taskMgmt.scheduleNew("Do this").andThen { case r =>

        val returnVal: ReturnVal[Long] = r.get
        returnVal.v.isLeft should beTrue

        taskPlan.loadFromHistory(returnVal.events)

        taskPlan.size should be_==(1)
        taskPlan.countLeftToComplete should be_==(1)
        taskPlan.markCommitted

        val task: Option[Task] = taskPlan.findById(returnVal.value)
        task.get.txt shouldEqual "Do this"

      } andThen { case _ =>

        taskMgmt.complete(taskOne) andThen { case r =>

          val events = r.get
          taskPlan.loadFromHistory(events)

          taskPlan.countLeftToComplete should be_==(0)

        }
      } andThen { case _ =>

        taskMgmt.clearCompletedTasks andThen { case r =>

          val events: Iterable[TaskEvent] = r.get
          taskPlan.loadFromHistory(events)

          taskPlan.countLeftToComplete should be_==(0)
          taskPlan.size should be_==(0)
        }
      }
    }

    /**
     *
     */
    "schedule two tasks and complete them separately" in new PlanScope {

      var task1: Option[Task] = None

      taskMgmt.scheduleNew("Do this") andThen { case r =>

        val returnVal: ReturnVal[Long] = r.get
        returnVal.v.isLeft should beTrue

        taskPlan.loadFromHistory(returnVal.events)
        taskPlan.size should be_==(1)
        taskPlan.countLeftToComplete should be_==(1)

        task1 = taskPlan.findById(returnVal.value)
        task1.get.txt shouldEqual "Do this"

      } andThen { case _ =>

        taskMgmt.redefine(task1.get.id.get, "Do this other thing") andThen { case r =>

          val history = r.get
          taskPlan.loadFromHistory(history)

          taskPlan.size should be_==(1)
          taskPlan.countLeftToComplete should be_==(1)

          assert(taskPlan.tasks.head.txt.equals("Do this other thing"))
          //taskPlan.tasks.head.txt should be_==("Do this other thing")

        }
      } andThen { case _ =>

        taskMgmt.complete(taskOne) andThen { case r =>

          val history = r.get
          taskPlan.loadFromHistory(history)

          taskPlan.countLeftToComplete should be_==(0)
        }

      } andThen { case _ =>

        taskMgmt.clearCompletedTasks andThen { case r =>

          val events: Iterable[TaskEvent] = r.get
          taskPlan.loadFromHistory(events)

          taskPlan.countLeftToComplete should be_==(0)
          taskPlan.size should be_==(0)
        }
      }
    }
  }
}
