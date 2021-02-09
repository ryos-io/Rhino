package client

import Client
import assertk.assertThat
import assertk.assertions.hasSize
import client.model.Request
import client.model.RequestSent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.slf4j.LoggerFactory
import kotlin.time.seconds

private val LOG = LoggerFactory.getLogger(ClientTest::class.java)

/**
 * Unit test for Client class.
 * If you intend to run your test in a TestCoroutineScope
 * make sure you mock everything that involves coroutines in a loop, like [Monitor].
 * Otherwise these coroutines won't end with the test.
 * Even exception will be ignored.
 *
 * @see <a href="https://github.com/Kotlin/kotlinx.coroutines/issues/1910">Github Issue</a>
 */
@ExtendWith(MockitoExtension::class)
class ClientTest {
    private val testCoroutineDispatcher = TestCoroutineDispatcher()
    private val testCoroutineScope = TestCoroutineScope(testCoroutineDispatcher)
    private val requests = mutableListOf<Request>()

    @Mock
    lateinit var monitor: Monitor;


    @Test
    fun `test constant rate of 1rps`(): Unit = testCoroutineScope.runBlockingTest {
        val client = Client(testCoroutineScope, monitor = monitor) {
            rateLimit {
                startRps = 1
                targetRps = 1
                timeSpan = 1000.seconds
            }

            addListener {
                when (it) {
                    is RequestSent -> requests.add(it.request)
                }
            }
        }
        // run multiple scenarios in order to hold request rate otherwise one scenario
        // could cause a drop when a request takes longer than the 1s interval
        repeat(3) {
            launch { scenario(client) }
        }
        repeat(9) {
            assertThat(requests).hasSize(it)
            advanceTimeBy(1000)
        }
    }

    @Test
    fun `test ramp up from 0 to 10 rps in 10s`(): Unit = testCoroutineScope.runBlockingTest {
        val client = Client(testCoroutineScope, monitor = monitor) {
            rateLimit {
                startRps = 0
                targetRps = 10
                timeSpan = 10.seconds
            }

            addListener {
                when (it) {
                    is RequestSent -> requests.add(it.request)
                }
            }
        }
        val requestsPerScenario = 3
        val scenarioRepeats = 200
        repeat(scenarioRepeats) {
            launch { scenario(client) }
        }
        var count = 0
        repeat(10) {
            count += it
            assertThat(requests).hasSize(count)
            advanceTimeBy(1000) // +1
        }
        // check if rate is hold
        assertThat(requests).hasSize(55)
        advanceTimeBy(1000)
        assertThat(requests).hasSize(65)
        advanceTimeBy(1000)
        assertThat(requests).hasSize(75)
        advanceTimeBy(1000)
    }

    suspend fun scenario(client: Client) = coroutineScope {
        val res1 = client.url("http://localhost:8080/foo").get()
        val res2 = client.url("http://localhost:8080/foo?page=$res1").get()
        val res3 = client.url("http://localhost:8080/foo?page=$res2").get()
    }
}