package teksol.postgres

import org.springframework.jdbc.core.JdbcTemplate
import teksol.infrastructure.{Event, EventBus, EventBusConsumer}

class PostgresEventBus(private[this] val jdbcTemplate: JdbcTemplate) extends EventBus {
    private[this] var consumers: Set[EventBusConsumer] = Set.empty

    override def publish(event: Event): Unit = {
        recordEvent(event)
        propagateEvent(event)
    }

    override def register(consumer: EventBusConsumer): Unit =
        consumers = consumers + consumer

    override def unregister(consumer: EventBusConsumer): Unit =
        consumers = consumers - consumer

    private[this] def propagateEvent(event: Event): Unit =
        consumers.foreach(_.receive(event))

    private[this] def recordEvent(event: Event) =
        jdbcTemplate.update("INSERT INTO public.events(event_type, contents) VALUES (?::text, ?::json)",
            event.getClass.getSimpleName, event.toJson)
}
