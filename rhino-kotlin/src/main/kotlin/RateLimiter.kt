import kotlinx.coroutines.*
import kotlin.time.Duration
import kotlin.time.milliseconds

private val INTERVAL = 500.milliseconds

// intended to be run on a single thread
class RateLimiter(
    private val startRps: Int,
    private val targetRps: Int,
    private val end: Duration,
    // scope to launch deferred process
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private var counter = 0L
    private var duration = Duration.ZERO

    private val slope: Double = (targetRps - startRps) / end.inSeconds

    suspend fun <R> rateLimit(
        runnable: suspend () -> R,
    ): Deferred<R> {
        // do request if limit not reached
        // if limit reached retry in next interval and reset counter
        while (true) {
            // rps = a * t + startRps
            val currentRps = slope * duration.inSeconds + startRps
            // 1 rps, 500ms interval => 0,5 requests per 500ms
            val requestPerInterval = (currentRps / 1000) * INTERVAL.inMilliseconds
            if (counter < requestPerInterval.toInt()) {
                counter++
                break
            } else {
                // suspend
                awaitNextInterval()
            }
        }
        // fire and forget
        // TODO is it needed to limit number of async requests?
        return scope.async { runnable() }
    }

    private suspend fun awaitNextInterval() {
        duration += INTERVAL
        counter = 0
        delay(INTERVAL)
    }
}