import client.model.Event
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration
import kotlin.time.milliseconds
import kotlin.time.seconds


private val LOG: Logger = LoggerFactory.getLogger("Foo4")

fun main() = runBlocking<Unit> {
    val simDuration = 100.seconds
    var requests = AtomicInteger(0)
    val client = Client {
        rateLimit {
            startRps = 200
            targetRps = 401
            timeSpan = simDuration
        }

        addListener {
            when (it) {
                is Event.RequestSent -> requests.getAndIncrement()
            }
        }
    }
    client.use {
        withTimeout(simDuration) {
            var interval = Duration.ZERO
            launch {
                var lastCount = 0
                while (true) {
                    delay(1000.milliseconds)
                    val count = requests.get()
                    val rps = count - lastCount
                    lastCount = count
                    LOG.debug("Current RPS: $rps")
                }
            }
            // TODO add worker queue
            launch {
                while (true) {
                    launch {
                        repeat(500) {
                            launch(Dispatchers.Default) {
                                client.url("http://localhost:8080/foo").get()
                            }
                        }
                    }
                    // allow to timeout (caps requests per second to
                    delay(100)
                }
            }
        }
    }
}