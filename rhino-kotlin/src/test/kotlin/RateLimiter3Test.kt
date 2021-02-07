import assertk.assertThat
import assertk.assertions.hasSize
import client.model.Event
import client.model.Request
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import kotlin.time.seconds

private val LOG = LoggerFactory.getLogger(RateLimiter3Test::class.java)

class RateLimiter3Test {
    private val testCoroutineDispatcher = TestCoroutineDispatcher()
    private val testCoroutineScope = TestCoroutineScope(testCoroutineDispatcher)
    private val requests = mutableListOf<Request>()

    @Test
    fun `test constant rate of 1rps`(): Unit = testCoroutineScope.runBlockingTest {
        val client = Client {
            rateLimit {
                startRps = 1
                targetRps = 1
                timeSpan = 10.seconds
            }

            addListener {
                when (it) {
                    is Event.RequestSent -> requests.add(it.request)
                }
            }
        }
        client.use {
            // run multiple scenarios in order to hold request rate otherwise one scenario
            // could cause a drop when a request takes longer than the 1s interval
            repeat(3) {
                launch { scenario(client) }
            }
            (1 until 10).forEach {
                assertThat(requests).hasSize(it)
                advanceTimeBy(1000)
            }
        }
    }

    suspend fun scenario(client: Client) = coroutineScope {
        val res1 = client.url("http://localhost:8080/foo").get()
        val res2 = client.url("http://localhost:8080/foo?page=$res1").get()
        val res3 = client.url("http://localhost:8080/foo?page=$res2").get()
    }
}