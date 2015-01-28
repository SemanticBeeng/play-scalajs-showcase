package example

import common.Framework
import org.scalajs.dom
import org.scalajs.dom.extensions.AjaxException
import shared.config.Routes
import shared.domain.todo.{Task, TodoSystemException, TodoBusinessException, TodoIntf}
import scala.util.{Failure, Success}
import scalatags.JsDom._
import all._
import tags2.section
import rx._
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.concurrent.Future


@JSExport
object TodoJS {

  import Framework._

  object Model {

    import scala.scalajs.js
    import js.Dynamic.{global => g}
    import org.scalajs.dom.extensions.Ajax
    import org.scalajs.jquery.{jQuery => $}
    import upickle._
    import common.ExtAjax._

    object TodoClient extends TodoIntf {

      def all: Future[List[Task]] = {
        Future(Model.tasks())
      }

      /**
       *
       */
      override def create(txt: String, done: Boolean): Future[Either[Task, TodoBusinessException]] = {

        val json = s"""{"txt": "${txt}", "done": ${done}}"""
        Ajax.postAsJson(Routes.Todos.create, json).map { r =>

          read[Either[Task, TodoBusinessException]](r.responseText)
        }.recover {
          // Trigger client side system exceptions
          case e: AjaxException => throw new TodoSystemException(e.xhr.responseText)
          case e1 => throw new TodoSystemException(e1.toString)
        }
      }

      /**
       *
       */
      override def update(task: Task): Future[Boolean] = {

        val json = s"""{"txt": "${task.txt}", "done": ${task.done}}"""
        //task.id.map{ id =>
        Ajax.postAsJson(Routes.Todos.update(task.id.get), json).map { r =>

          read[Boolean](r.responseText)
        }.recover {
          // Trigger client side system exceptions
          case e: AjaxException => throw new TodoSystemException(e.xhr.responseText)
          case e1 => throw new TodoSystemException(e1.toString)
        }
      }

      /**
       *
       */
      override def delete(id: Long): Future[Boolean] = {
        Ajax.delete(Routes.Todos.delete(id)).map { r =>

          read[Boolean](r.responseText)
        }.recover {
          // Trigger client side system exceptions
          case e: AjaxException => throw new TodoSystemException(e.xhr.responseText)
          case e1 => throw new TodoSystemException(e1.toString)
        }
      }

      /**
       *
       */
      override def clearCompletedTasks: Future[Boolean] = {
        Ajax.postAsForm(Routes.Todos.clear).map { r =>

          read[Boolean](r.responseText)
        }.recover {
          // Trigger client side system exceptions
          case e: AjaxException => throw new TodoSystemException(e.xhr.responseText)
          case e1 => throw new TodoSystemException(e1.toString)
        }
      }
    }

    val tasks = Var(List.empty[Task])

    val done = Rx {
      tasks().count(_.done)
    }

    val notDone = Rx {
      tasks().length - done()
    }

    val editing = Var[Option[Task]](None)

    val filter = Var("All")

    val filters = Map[String, Task => Boolean](
      ("All", t => true),
      ("Active", !_.done),
      ("Completed", _.done)
    )

    def init: Future[Unit] = {
      Ajax.get(Routes.Todos.all).map { r =>
        read[List[Task]](r.responseText)
      }.map { r =>
        tasks() = r
      }
    }

    /**
     *
     */
    def all: Future[List[Task]] = TodoClient.all

    /**
     *
     */
    def create(txt: String, done: Boolean = false) = {

      TodoClient.create(txt, done).onComplete {

        case Success(result) =>
          if (result.isLeft) {
            tasks() = result.left.get +: tasks()
          }
          else {
            dom.alert(result.right.get.message)
          }
        case Failure(e) => dom.alert("create failed: " + e.getMessage)
      }
    }

    /**
     *
     */
    def update(task: Task) = {

      TodoClient.update(task).onComplete {

        case Success(_) =>
          val pos = tasks().indexWhere(t => t.id == task.id)
          tasks() = tasks().updated(pos, task)

        //case Success(false) => dom.alert("update failed")
        case Failure(e) => dom.alert("update failed: " + e.toString)
      }
    }

    /**
     *
     */
    def delete(idOp: Option[Long]) = {
      idOp.map { id =>
        TodoClient.delete(id).onComplete {

          case Success(_) =>
            tasks() = tasks().filter(_.id != idOp)

          //case Success(false) => dom.alert("delete failed")
          case Failure(e) => dom.alert("delete failed: " + e.getMessage)
        }
      }
    }

    /**
     *
     */
    def clearCompletedTasks() = {
      TodoClient.clearCompletedTasks.onComplete {

        case Success(true) =>
          tasks() = tasks().filter(!_.done)

        case Success(false) => dom.alert("clearCompletedTasks failed")
        case Failure(e) => dom.alert("clearCompletedTasks failed: " + e.getMessage)
      }
    }

  }

  val inputBox = input(
    id := "new-todo",
    placeholder := "What needs to be done?",
    autofocus := true
  ).render

  def templateHeader = {
    header(id := "header")(
      form(
        inputBox,
        onsubmit := { () =>
          Model.create(inputBox.value)
          inputBox.value = ""
          false
        }
      )
    )
  }

  def templateBody = {
    section(id := "main")(
      input(
        id := "toggle-all",
        `type` := "checkbox",
        cursor := "pointer",
        onclick := { () =>
          val target = Model.tasks().exists(_.done == false)
          //          Var.set(tasks().map(_.done -> target): _*)
        }
      ),
      label(`for` := "toggle-all", "Mark all as complete"),
      partList,
      partControls
    )
  }

  def templateFooter = {
    footer(id := "info")(
      p("Double-click to edit a todo"),
      p("Original version created by ", a(href := "https://github.com/lihaoyi/workbench-example-app/blob/todomvc/src/main/scala/example/ScalaJSExample.scala")("Li Haoyi")),
      p("Modified version with database backend can be found ", a(href := "https://github.com/hussachai/play-scalajs-showcase")("here"))
    )
  }

  def partList = Rx {
    ul(id := "todo-list")(
      for (task <- Model.tasks() if Model.filters(Model.filter())(task)) yield {
        val inputRef = input(`class` := "edit", value := task.txt).render

        li(
          `class` := Rx {
            if (task.done) "completed"
            else if (Model.editing() == Some(task)) "editing"
            else ""
          },
          div(`class` := "view")(
            "ondblclick".attr := { () =>
              Model.editing() = Some(task)
            },
            input(`class` := "toggle", `type` := "checkbox", cursor := "pointer", onchange := { () =>
              Model.update(task.copy(done = !task.done))
            }, if (task.done) checked := true else ""
            ),
            label(task.txt),
            button(
              `class` := "destroy",
              cursor := "pointer",
              onclick := { () => Model.delete(task.id)}
            )
          ),
          form(
            onsubmit := { () =>
              Model.update(task.copy(txt = inputRef.value))
              Model.editing() = None
              false
            },
            inputRef
          )
        )
      }
    )
  }

  def partControls = {
    footer(id := "footer")(
      span(id := "todo-count")(strong(Model.notDone), " item left"),
      ul(id := "filters")(
        for ((name, pred) <- Model.filters.toSeq) yield {
          li(a(
            `class` := Rx {
              if (name == Model.filter()) "selected"
              else ""
            },
            name,
            href := "#",
            onclick := { () => Model.filter() = name}
          ))
        }
      ),
      button(
        id := "clear-completed",
        onclick := { () => Model.clearCompletedTasks},
        "Clear completed (", Model.done, ")"
      )
    )
  }

  @JSExport
  def main(): Unit = {

    Model.init.map { r =>
      dom.document.getElementById("content").appendChild(
        section(id := "todoapp")(
          templateHeader,
          templateBody,
          templateFooter
        ).render
      )
    }
  }


}