import assertk.assertThat
import assertk.assertions.hasSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import kotlin.random.Random

private val LOG = LoggerFactory.getLogger(RateLimiter2Test::class.java)

class RateLimiter2Test {
    private val testCoroutineDispatcher = TestCoroutineDispatcher()
    private val testCoroutineScope = TestCoroutineScope(testCoroutineDispatcher)
    private val requests = mutableListOf<Int>()

    @Test
    fun `test constant rate of 1rps`(): Unit = testCoroutineScope.runBlockingTest {
        val rateLimit = rateLimit()
        repeat(3) {
            launch { scenario(rateLimit) }
        }
        assertThat(requests).hasSize(1)
        advanceTimeBy(1000)
        assertThat(requests).hasSize(2)
        advanceTimeBy(1000)
        assertThat(requests).hasSize(3)
        rateLimit.cancel()
    }

    fun CoroutineScope.rateLimit() = produce {
        var x = 1
        while (true) {
            send(x++)
            delay(1000)
        }
    }

    suspend fun doRequest(channel: ReceiveChannel<Int>, input: Long = 0): Long = coroutineScope {
        val id = channel.receive()
        requests.add(id)
        LOG.debug("doing request $id based on $input")
        val timeMillis = Random.nextLong(2000, 3000)
        delay(timeMillis)
        timeMillis
    }

    suspend fun scenario(channel: ReceiveChannel<Int>) = coroutineScope {
        val res1 = doRequest(channel)
        val res2 = doRequest(channel, res1)
        val res3 = doRequest(channel, res2)
    }
}