package client

import client.model.Event
import client.model.RequestSent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.milliseconds
import kotlin.time.seconds

private val LOG = LoggerFactory.getLogger(Monitor::class.java)

class Monitor(scope: CoroutineScope, interval: Duration = 1000.milliseconds) : AutoCloseable {
    private var start: Instant = Instant.MIN
    private var currentTotalRequests = 0
    private var currentDuration = 0.seconds
    private var _status = Triple(0, currentTotalRequests, 0.seconds)

    private val eventConsumer: SendChannel<Event> = scope.actor(capacity = 1000) {
        consumeEach {
            LOG.debug("Got event: $it")
            when (it) {
                is RequestSent -> {
                    if (start == Instant.MIN) {
                        start = Instant.now()
                    }
                    currentTotalRequests++
                }
            }
        }
    }

    private val monitorJob =
        scope.launch {
            while (coroutineContext.isActive) {
                val lastTotalRequests = _status.second
                val rps = (currentTotalRequests - lastTotalRequests)
                _status = Triple(rps, currentTotalRequests, currentDuration)
                LOG.debug("Calculated status: $_status")
                currentDuration += 1.seconds
                delay(interval)
            }
        }

    val status: Triple<Int, Int, Duration>
        get() = _status

    suspend fun send(e: Event) {
        eventConsumer.send(e)
    }

    override fun close() {
        eventConsumer.close()
        monitorJob.cancel()
    }
}