import assertk.assertThat
import assertk.assertions.hasSize
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

private val LOG = LoggerFactory.getLogger(RateLimiter3Test::class.java)

class RateLimiter3Test {
    private val testCoroutineDispatcher = TestCoroutineDispatcher()
    private val testCoroutineScope = TestCoroutineScope(testCoroutineDispatcher)
    private val requests = mutableListOf<Int>()

    @Test
    fun `test constant rate of 1rps`(): Unit = testCoroutineScope.runBlockingTest {
        val client = Client {
            rampUp {
                startRps = 1
                targetRps = 1
                timespan = 10.seconds
            }
        }
        repeat(3) {
            launch { scenario(client) }
        }
        assertThat(requests).hasSize(1)
        advanceTimeBy(1000)
        assertThat(requests).hasSize(2)
        advanceTimeBy(1000)
        assertThat(requests).hasSize(3)
        client.close()
    }

    suspend fun scenario(client: Client) = coroutineScope {
        val res1 = client.url("http://localhost:8080/foo").get()
        val res2 = client.url("http://localhost:8080/foo?page=$res1").get()
        val res3 = client.url("http://localhost:8080/foo?page=$res2").get()
    }
}