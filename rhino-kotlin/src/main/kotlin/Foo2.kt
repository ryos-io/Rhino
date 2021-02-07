import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random
import kotlin.time.milliseconds
import kotlin.time.seconds


private val LOG: Logger = LoggerFactory.getLogger("Foot2")
private val counter = AtomicInteger(0)

suspend fun doRequest() {
    val id = counter.getAndIncrement()
    LOG.debug("started request $id")
    delay(Random.nextLong(200, 1000))
//    LOG.debug("finished request $id")
}

fun main() = runBlocking<Unit> {
    val duration = 60.seconds
    val rateLimiter = RateLimiter(300, 300, end = 10.seconds)
    launch {
        withTimeout(duration) {
            while (true) {
                rateLimiter.rateLimitAsync {
                    doRequest()
                }
                // allow to timeout
                delay(5.milliseconds)
            }
        }
    }
}