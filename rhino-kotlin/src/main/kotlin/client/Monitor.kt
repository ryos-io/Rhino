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

data class Status(val rps: Int, val totalRequests: Int, val clock: Duration)

class Monitor(scope: CoroutineScope, interval: Duration = 1000.milliseconds) : AutoCloseable {
    private var start: Instant = Instant.MIN
    private var currentTotalRequests = 0
    private var currentDuration = 0.seconds
    private var _status = Status(0, currentTotalRequests, 0.seconds)

    private val eventConsumer: SendChannel<Event> = scope.actor(capacity = 1000) {
        consumeEach {
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
                // TODO calc real delay
                delay(interval)
                currentDuration += interval
                val lastTotalRequests = _status.totalRequests
                val requestsPerInterval = (currentTotalRequests - lastTotalRequests)
                val rps = ((requestsPerInterval / interval.inMilliseconds) * 1000).toInt()
                _status = Status(rps, currentTotalRequests, currentDuration)
                LOG.info("$_status")
            }
        }

    val status: Status
        get() = _status

    suspend fun send(e: Event) {
        eventConsumer.send(e)
    }

    override fun close() {
        eventConsumer.close()
        monitorJob.cancel()
    }
}