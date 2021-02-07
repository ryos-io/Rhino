import assertk.assertThat
import assertk.assertions.hasSize
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.time.milliseconds
import kotlin.time.seconds

class RateLimiterTest {
    private val testCoroutineDispatcher = TestCoroutineDispatcher()
    private val testCoroutineScope = TestCoroutineScope(testCoroutineDispatcher)
    var counter: Int = 0

    suspend fun doRequest(): Int = coroutineScope {
        val id = counter++
        // don't use logs they just confuse since delay doesn't suspend actually in the TestCoroutineScope
//        log("doing something ($id)")
        delay(Random.nextLong(200, 300))
//        log("did something ($id)")
        id
    }

    @Test
    fun `test ramp up from 1 to 10 rps`(): Unit = testCoroutineScope.runBlockingTest {
        val requests = mutableListOf<Deferred<Int>>()
        val limiter =
            RateLimiter(
                startRps = 1,
                targetRps = 11,
                end = 10.seconds,
                interval = 500.milliseconds,
                testCoroutineScope
            )
        launch {
            repeat(1000) {
                val request = limiter.rateLimitAsync { doRequest() }
                requests.add(request)
            }
        }
        assertThat(requests).hasSize(0)
        advanceTimeBy(1000)
        assertThat(requests).hasSize(1)
        advanceTimeBy(1000)
        assertThat(requests).hasSize(3)
        advanceTimeBy(1000)
        assertThat(requests).hasSize(6)
    }

    @Test
    @Disabled
    fun `test startRps=targetRps`(): Unit = testCoroutineScope.runBlockingTest {
        val requests = mutableListOf<Deferred<Int>>()
        val limiter =
            RateLimiter(
                startRps = 1,
                targetRps = 1,
                end = 10.seconds,
                interval = 500.milliseconds,
                testCoroutineScope
            )
        launch {
            repeat(1000) {
                val request = limiter.rateLimitAsync { doRequest() }
                requests.add(request)
            }
        }
        assertThat(requests).hasSize(0)
        advanceTimeBy(1000)
        assertThat(requests).hasSize(1)
        advanceTimeBy(1000)
        assertThat(requests).hasSize(2)
        advanceTimeBy(1000)
        assertThat(requests).hasSize(3)
        advanceTimeBy(1000)
        assertThat(requests).hasSize(4)
    }
}