# Play Framework with Scala.js Showcase


## "Business" purpose
Evolves the original code of this TypeSafe template to demonstrate the use a of a shared business interface for the
Todo application.

This approach is designed to enable writing of business logic in a way that is transparent to the layer and to execute such business logic on any tier as appropriate. This is necessary because business logic cross cuts the layers : client, server, data access layer, UI, etc

This achieved by creating TodoClient and a TodoServer that share the same API, TodoIntf.

The question is what is the role of Play! controllers in this context

1. This approach uses Play! controllers for marshalling

1. As opposed to using AutoWire, this approach preserves the Play! strongly typed controllers.

This way, the semantics/functionality behind the shared business API can be consumed through

1. Directly through the TodoIntf and TodoClient or

1. Through the Play! controllers which provide a very closely related, even though slightly different, web like API.

## Technical purpose

Demonstrates

1. Use of "latest" (http://www.scala-js.org/news/2015/01/23/announcing-scalajs-0.6.0-RC2/) ScalaJS (see code diffs
for slight API changes in jquery, for example)
1. Use of PlayScalaJS SBT plugin for cross building


Debug with

$ sbt -jvm-debug 9999 run
