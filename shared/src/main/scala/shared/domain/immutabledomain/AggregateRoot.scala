package shared.domain.immutabledomain

import scala.collection._

trait AggregateRoot[Event] {
  protected def applyEvent: Event => Unit

  def uncommittedEvents: Iterable[Event] = _uncommittedEvents.clone()

  def markCommitted = _uncommittedEvents.clear

  def loadFromHistory(history: Iterable[Event]) = history.foreach(applyEvent)

  /*@todo why protected ? protected */def record(event: Event) {
    applyEvent(event)
    _uncommittedEvents += event
  }

  private val _uncommittedEvents = mutable.Queue[Event]()
}
