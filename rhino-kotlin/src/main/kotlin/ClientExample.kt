import client.model.RequestSent
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.milliseconds
import kotlin.time.seconds


private val LOG: Logger = LoggerFactory.getLogger("ClientExample")

fun main() = runBlocking<Unit> {
    val simDuration = 60.seconds
    withTimeout(simDuration) {
        val requests = AtomicInteger(0)
        val client = Client(this) {
            rateLimit {
                startRps = 360
                targetRps = 360
                timeSpan = simDuration
            }

            addListener {
                when (it) {
                    is RequestSent -> requests.getAndIncrement()
                }
            }
        }
        var lastTime = Instant.now()
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
                LOG.debug("Current RPS: ${rps.toInt()}, total: $count")
            }
        }
        val parallelScenarios = 1000
        repeat(parallelScenarios) {
            launch(CoroutineName("scenario") + Dispatchers.Default) {
                LOG.debug("started sceario $it")
                while (true) {
                    client.url("http://localhost:8080/foo").get()
                }
            }
        }
    }
}