package showcase.tests

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import org.specs2.specification.Scope
import shared.domain.todo._
import shared.mock.TodoServerMock

import scala.concurrent.Future

//import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @todo use "fixtures" to provide test data, "Outside" and "context composition"
 *       http://etorreborre.github.io/specs2/guide/org.specs2.guide.Structure.html
 *       http://etorreborre.github.io/specs2/guide/org.specs2.guide.structure.GivenWhenThenPage.html
 *
 * @todo combine this with Play! specs
 *       https://groups.google.com/forum/#!topic/specs2-users/HkPJqH83I5Y
 *
 */
@RunWith(classOf[JUnitRunner])
class BusinessSpec extends Specification {

  trait PlanScope extends Scope {

    val taskOne = TaskId(1L)

    val taskPlan = new Plan
    val taskMgmt: TaskManagement = new TodoServerMock()

    var lastTaskUsed: Option[Task] = None
  }

  class PlanModelProxy extends PlanScope {

    /**
     *
     */
    def do_scheduleNew(txt: String): Future[ReturnVal[TaskId]] = {

      val planSizeBefore = taskPlan.size
      val countLeftToCompleteBefore = taskPlan.countLeftToComplete

      val future: Future[ReturnVal[TaskId]] = taskMgmt.scheduleNew(txt)
      future map { returnVal =>

        returnVal.v.isLeft should beTrue

        taskPlan.loadFromHistory(returnVal.events)

        taskPlan.size should be_==(planSizeBefore + 1)
        taskPlan.countLeftToComplete should be_==(countLeftToCompleteBefore + 1)
        taskPlan.markCommitted

        lastTaskUsed = taskPlan.findById(returnVal.value)
        lastTaskUsed.get.txt shouldEqual txt

      }
      future
    }

    /**
     *
     */
    def do_complete(taskId: TaskId): Future[Iterable[TaskEvent]] = {

      val planSizeBefore = taskPlan.size
      val countLeftToCompleteBefore = taskPlan.countLeftToComplete

      val future: Future[Iterable[TaskEvent]] = taskMgmt.complete(taskId)

      future andThen { case r =>

        val events = r.get
        taskPlan.loadFromHistory(events)

        taskPlan.size should be_==(planSizeBefore)
        taskPlan.countLeftToComplete should be_==(countLeftToCompleteBefore - 1)

      }

      future
    }

    /**
     *
     */
    def do_clearCompleted: Future[Iterable[TaskEvent]] = {

      val future: Future[Iterable[TaskEvent]] = taskMgmt.clearCompletedTasks
      future andThen { case r =>

        val events: Iterable[TaskEvent] = r.get
        taskPlan.loadFromHistory(events)

        taskPlan.countLeftToComplete should be_==(0)
        taskPlan.size should be_==(0)
      }

      future
    }
  }

  "I should be able to" should {

    /**
     *
     */
    "schedule one task, redefine and complete it" in new PlanScope {

      taskPlan.loadFromHistory(Seq(
        TaskScheduled(Task(taskOne, "Do this")),
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
        TaskScheduled(Task(taskOne, "Do this"))))

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
    "schedule one task and complete it remotely" in new PlanModelProxy {

      do_scheduleNew("Do this") andThen { case _ =>

        do_complete(lastTaskUsed.get.id) andThen { case _ =>

          do_clearCompleted
        }
      }
    }

    /**
     *
     */
    "schedule two tasks and complete them separately" in new PlanModelProxy {


      do_scheduleNew("Do this") andThen { case _ =>

        taskMgmt.redefine(lastTaskUsed.get.id, "Do this other thing") map { events =>

          taskPlan.loadFromHistory(events)

          taskPlan.size should be_==(1)
          taskPlan.countLeftToComplete should be_==(1)

          assert(taskPlan.tasks.head.txt.equals("Do this other thing"))
          //taskPlan.tasks.head.txt should be_==("Do this other thing")

        }
      } andThen { case _ =>

        taskMgmt.complete(taskOne) map { events =>

          taskPlan.loadFromHistory(events)

          taskPlan.countLeftToComplete should be_==(0)
        }

      } andThen { case _ =>

        taskMgmt.clearCompletedTasks map { events =>

          taskPlan.loadFromHistory(events)

          taskPlan.countLeftToComplete should be_==(0)
          taskPlan.size should be_==(0)
        }
      }
    }

    /**
     *
     */
  }
}