package teksol.infrastructure

trait EventBus {
    def register(consumer: EventBusConsumer)

    def unregister(consumer: EventBusConsumer)

    def publish(event: Event)
}
