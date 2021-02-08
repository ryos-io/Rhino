import client.model.Event
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.milliseconds
import kotlin.time.seconds


private val LOG: Logger = LoggerFactory.getLogger("Foo4")

fun main() = runBlocking<Unit> {
    val simDuration = 100.seconds
    val requests = AtomicInteger(0)
    val client = Client {
        rateLimit {
            startRps = 200
            targetRps = 300
            timeSpan = simDuration
        }

        addListener {
            when (it) {
                is Event.RequestSent -> requests.getAndIncrement()
            }
        }
    }
    client.use {
        var lastTime = Instant.now()
        withTimeout(simDuration) {
            launch {
                var lastCount = 0
                while (true) {
                    delay(1000.milliseconds)
                    val now = Instant.now()
                    val count = requests.get()
                    val intervalMs = Duration.between(lastTime, now).toMillis().toDouble()
                    lastTime = now
                    val rps = ((count - lastCount) / intervalMs) * 1000
                    lastCount = count
                    LOG.debug("Current RPS: $rps, total: $count")
                }
            }
            // TODO add worker queue
            launch {
                while (true) {
                    repeat(1000) {
                        launch(Dispatchers.IO) {
                            client.url("http://localhost:8080/foo").get()
                        }
                    }
                    // allow to timeout (caps requests per second to
                    delay(100)
                }
            }
        }
    }
}