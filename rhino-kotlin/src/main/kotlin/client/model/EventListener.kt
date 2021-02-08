package client.model

fun interface EventListener {
    fun onEvent(e: Event)
}