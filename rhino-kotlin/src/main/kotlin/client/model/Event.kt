package client.model

sealed class Event {
    class RequestSent(val request: Request) : Event()
}