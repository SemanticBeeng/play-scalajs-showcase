# Play Framework with Scala.js Showcase

Evolves the original code to demonstrate the use a of a shared business interface for the Todo application.

The intent is to create a TodoClient and a TodoServer that share the same API, TodoIntf.

This uses use Play! controllers only for marshalling,

As opposed to using AutoWire, this approach preserves the Play! strongly typed controllers so callers of these controllers can access the same functionality, even if through a slightly different, web like API.

