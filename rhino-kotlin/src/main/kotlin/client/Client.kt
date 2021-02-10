import client.Monitor
import client.model.EventListener
import client.model.Request
import client.model.RequestSent
import client.model.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.slf4j.LoggerFactory
import java.lang.Double.min
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.milliseconds
import kotlin.time.minutes
import kotlin.time.seconds

private val LOG = LoggerFactory.getLogger(Client::class.java)

@DslMarker
annotation class ConfigDsl

@ConfigDsl
class Config(
    val rateLimit: RateLimit, val listeners: List<EventListener> = listOf()
) {
    class RateLimit(val startRps: Int, val targetRps: Int, val timeSpan: Duration) {
        class Builder {
            var startRps = 100
            var targetRps = 500
            var timeSpan = 5.minutes
            fun build() = RateLimit(startRps, targetRps, timeSpan)
        }
    }

    class Builder {
        private val listeners: MutableList<EventListener> = mutableListOf()
        private var rateLimitBuilder: RateLimit.Builder = RateLimit.Builder()


        fun rateLimit(configure: RateLimit.Builder.() -> Unit) {
            rateLimitBuilder = RateLimit.Builder().apply(configure)
        }

        fun addListener(listener: EventListener) {
            listeners.add(listener)
        }

        fun build() = Config(rateLimitBuilder.build(), listeners)
    }
}

class Client internal constructor(
    private val config: Config,
    private val rateLimiter: ReceiveChannel<Int>,
    val monitor: Monitor,
) : AutoCloseable {
    inner class RequestBuilder {
        suspend fun get(): Response = coroutineScope {
            rateLimiter.receive()
            val requestSent = RequestSent(Request())

            monitor.onEvent(requestSent)
            config.listeners.forEach {
                it.onEvent(requestSent)
            }
//            LOG.debug("doing some request")
            delay(Random.nextLong(100, 500))
            Response()
        }
    }

    fun url(url: String) = RequestBuilder()

    override fun close() {
        rateLimiter.cancel()
        monitor.close()
    }
}

// create client in a coroutine scope so that the underlying channel is managed
// which means it will be canceled automatically when something wents wrong
fun Client(scope: CoroutineScope, monitor: Monitor = Monitor(scope)): Client {
    return Client(scope, monitor) {}
}

fun Client(
    scope: CoroutineScope,
    monitor: Monitor = Monitor(scope),
    configure: Config.Builder.() -> Unit
): Client {
    val config = Config.Builder().apply(configure).build()

    // TODO either fix interval bug (currently it has to be 1s) or just use 1s
    val rateLimiter = scope.produce(capacity = 1000) {
        val rateConfig = config.rateLimit
        val interval: Duration = 1000.milliseconds
        val slopePerMs: Double =
            ((rateConfig.targetRps - rateConfig.startRps) / rateConfig.timeSpan.inSeconds) / 1000.0
        val startRatePerInterval = (rateConfig.startRps / 1000.toDouble()) * interval.inMilliseconds
        val targetRatePerInterval =
            if (slopePerMs != 0.toDouble()) (rateConfig.targetRps / 1000.toDouble()) * interval.inMilliseconds else startRatePerInterval
        var counter = 0
        var duration = Duration.ZERO
        var lastDuration = Duration.ZERO

        // TODO refactor slope==0 and slope!=0 into different components
        while (coroutineContext.isActive) {
            // durationMs/intervalMs => iteration
            // startRatesPerInterval + slope * iteration
            val requestPerInterval = if (slopePerMs != 0.toDouble()) {
                min(
                    (slopePerMs * duration.inMilliseconds) + startRatePerInterval,
                    targetRatePerInterval
                )
            } else {
                startRatePerInterval * (duration.inMilliseconds / interval.inMilliseconds)
            }
            val requests = requestPerInterval.toInt()
            if (counter < requests) {
//                LOG.debug("counter: $counter, allowed requests: $requests, duration: $duration")
                repeat(requests) {
                    counter++
                    send(counter)
                }
                if (slopePerMs == 0.toDouble()) {
                    duration = Duration.ZERO
                } else {
                    duration += interval
                    delay(interval)
                }
            } else {
                duration += interval
                delay(interval)
            }
            if (slopePerMs != 0.0) {
                if (duration - lastDuration >= 1.seconds) {
                    counter = 0
                    lastDuration = duration
                }
            } else {
                counter = 0
            }
        }
    }
    return Client(config, rateLimiter, monitor)
}
