import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.io.Closeable
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.minutes

@DslMarker
annotation class ConfigDsl

@ConfigDsl
class Config(
    val rateLimit: RateLimit, val listeners: List<(Event) -> Unit> = listOf()
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
        private val listeners: MutableList<(Event) -> Unit> = mutableListOf()
        private var rateLimitBuilder: RateLimit.Builder = RateLimit.Builder()


        fun rateLimit(configure: RateLimit.Builder.() -> Unit) {
            rateLimitBuilder = RateLimit.Builder().apply(configure)
        }

        fun addListener(listener: (Event) -> Unit) {
            listeners.add(listener)
        }

        fun build() = Config(rateLimitBuilder.build(), listeners)
    }
}

class Request
class Response

sealed class Event {
    class RequestSent(val request: Request) : Event()
}

class Client internal constructor(val config: Config, val rateLimiter: ReceiveChannel<Int>) :
    Closeable {

    inner class RequestBuilder {
        suspend fun get(): Response = coroutineScope {
            rateLimiter.receive()
            config.listeners.forEach {
                it(Event.RequestSent(Request()))
            }
            delay(Random.nextLong(100, 500))
            Response()
        }
    }

    fun url(url: String) = RequestBuilder()

    override fun close() {
        rateLimiter.cancel()
    }
}

fun CoroutineScope.Client(configure: Config.Builder.() -> Unit): Client {
    val rateLimiter = produce {
        var x = 1
        while (true) {
            send(x++)
            delay(1000)
        }
    }
    return Client(Config.Builder().apply(configure).build(), rateLimiter)
}



