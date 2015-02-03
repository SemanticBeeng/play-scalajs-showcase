package showcase.tests

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import org.specs2.specification.Scope
import scala.concurrent.Future
import shared.domain.TaskManagementModelProxy
import shared.domain.todo._

private class TaskManagementModelProxySpecScope extends TaskManagementModelProxy with Scope

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
class TaskManagementSpec extends Specification {

  // Specs2 passes the execution context
  //val c = scala.concurrent.ExecutionContext.Implicits.global

  "I should be able to" should {

    /**
     *
     */
    "schedule one task, redefine and complete it" in new TaskManagementModelProxySpecScope {

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
    "schedule one task and cancel it" in new TaskManagementModelProxySpecScope {

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
    "schedule one task and complete it remotely" in new TaskManagementModelProxySpecScope {

      do_scheduleNew("Do this") andThen { case r =>

        val taskId = r.get.value
        do_complete(taskId)

      } andThen { case _ =>

        do_clearCompletedTasks
      }
    }

    /**
     *
     */
    "schedule two tasks and complete them separately" in new TaskManagementModelProxySpecScope {


      private val workWithTask1: Future[ReturnVal[TaskId]] =

        do_scheduleNew("Do this") andThen { case r =>

          val taskId = r.get.value
          val newDescription: String = "Do this very well!"
          do_redefine(taskId, newDescription) andThen { case _ =>

            assert(taskPlan.findById(taskId).get.txt.equals(newDescription))
            do_complete(taskId)
          }
        }

      private val workWithTask2: Future[ReturnVal[TaskId]] =

        do_scheduleNew("Do this thing too") andThen { case r =>

          val taskId = r.get.value
          val newDescription: String = "Do this very well also!"
          do_redefine(taskId, "Do this very well also!") andThen { case _ =>

            assert(taskPlan.findById(taskId).get.txt.equals(newDescription))
            do_complete(taskId)
          }
        }

      workWithTask1 andThen { case _ =>

        workWithTask2 andThen { case _ =>

          do_clearCompletedTasks
        }
      }
    }

    /**
     *
     */
  }
}