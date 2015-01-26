# Play Framework with Scala.js Showcase

Evolves the original code to demonstrate the use a of a shared business interface for the Todo application.

This approach is designed to enable writing of business logic in a way that is transparent to the layer and to execute such business logic on any tier as appropriate. This is necessary because business logic cross cuts the layers : client, server, data access layer, UI, etc

This achieved by creating TodoClient and a TodoServer that share the same API, TodoIntf.

The question is what is the role of Play! controllers in this context

1. This approach uses Play! controllers for marshalling

1. As opposed to using AutoWire, this approach preserves the Play! strongly typed controllers.

This way, the semantics/functionality behind the shared business API can be consumed through

1. Directly through the TodoIntf and TodoClient or

1. Through the Play! controllers which provide a very closely related, even though slightly different, web like API.

Debug with

$ sbt -jvm-debug 9999 run
