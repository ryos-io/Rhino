package client

import client.model.Event
import client.model.EventListener
import client.model.RequestSent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration
import kotlin.time.milliseconds
import kotlin.time.seconds

private val LOG = LoggerFactory.getLogger(Monitor::class.java)

data class Status(val rps: Int, val totalRequests: Int, val clock: Duration)

val defaultScope = CoroutineScope(
    Executors.newSingleThreadExecutor().asCoroutineDispatcher()
            + CoroutineName("Monitor")
)

class Monitor(
    private val scope: CoroutineScope = defaultScope,
    interval: Duration = 1000.milliseconds,
    private val queueSize: Int = 1000
) :
    AutoCloseable, EventListener {
    private var start: Instant = Instant.MIN
    private var currentTotalRequests = 0
    private var currentDuration = 0.seconds
    private var _status = Status(0, currentTotalRequests, 0.seconds)
    private var inflightEvents = AtomicInteger(0)

    private val eventConsumer: SendChannel<Event> = scope.actor(capacity = queueSize) {
        consumeEach {
            when (it) {
                is RequestSent -> {
                    if (start == Instant.MIN) {
                        start = Instant.now()
                    }
                    currentTotalRequests++
                    inflightEvents.getAndDecrement()
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
                // TODO pretty print to stdout
                LOG.info("$_status")
            }
        }

    val status: Status
        get() = _status

    override fun onEvent(e: Event) {
        // TODO test infight event mechanism (throw excepton that can be asserted)
        if (inflightEvents.getAndIncrement() < queueSize) {
            this.scope.async { eventConsumer.send(e) }
        } else {
            LOG.warn("It's over nine thousaaaaaaaaaand RPS (or something like that, can't keep up...)")
            inflightEvents.getAndDecrement()
        }
    }

    override fun close() {
        eventConsumer.close()
        monitorJob.cancel()
    }
}