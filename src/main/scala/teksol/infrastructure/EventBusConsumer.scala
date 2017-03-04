package teksol.infrastructure

trait EventBusConsumer {
    def receive(event: Event): Unit
}
