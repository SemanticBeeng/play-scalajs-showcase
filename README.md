# Play Framework with Scala.js Showcase

Evolves the original code to demonstrate the use a of a shared business interface for the Todo application.

The intent is to create a TodoClient and a TodoServer that share the same API, TodoIntf.

The question is what is the role of Play! controllers in this context

1. This approach uses Play! controllers for marshalling

1. As opposed to using AutoWire, this approach preserves the Play! strongly typed controllers.

This way, the semantics/functionality behind the shared business API can be consumed through

1. Directly through the TodoIntf and TodoClient or

1. Through the Play! controllers which provide a very closely related, even though slightly different, web like API.

