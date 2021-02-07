import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.lang.Double.min
import kotlin.time.Duration
import kotlin.time.milliseconds

private val LOG = LoggerFactory.getLogger(RateLimiter::class.java)

// NOT THREAD SAFE - intended to be run on a single thread
// notes:
// - calculation takes too much time to reach higher rps (>120)
// - can only rate limit independent requests
class RateLimiter(
    private val startRps: Int,
    private val targetRps: Int,
    private val end: Duration,
    private val interval: Duration = 100.milliseconds,
    // scope to launch deferred process
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val targetRate = (targetRps / 1000.toDouble()) * interval.inMilliseconds
    private var counter = 0L
    private var duration = Duration.ZERO

    private val slope: Double = (targetRps - startRps) / end.inSeconds

    suspend fun <R> rateLimitAsync(
        runnable: suspend () -> R,
    ): Deferred<R> {
        // do request if limit not reached
        // if limit reached retry in next interval and reset counter
        while (true) {
            val requestPerInterval = if (slope < 1.toDouble()) {
                // TODO fix
                throw IllegalStateException("constant rate it not implemented yet")
            } else {
                calcRequestsPerInterval()
            }
            if (counter < requestPerInterval.toInt()) {
                counter++
                break
            } else {
                LOG.debug("suspending...")
                // suspend
                awaitNextInterval()
            }
        }
        // fire and forget
        // TODO is it needed to limit number of async requests?
        return scope.async { runnable() }
    }

    private fun calcRequestsPerInterval(): Double {
        // rps = a * t + startRps
        val currentRps = min((slope * duration.inSeconds) + startRps, targetRps.toDouble())
        // 1 rps, 500ms interval => 0,5 requests per 500ms
        return (currentRps / 1000) * interval.inMilliseconds
    }

    private suspend fun awaitNextInterval() {
        duration += interval
        counter = 0
        delay(interval)
    }
}