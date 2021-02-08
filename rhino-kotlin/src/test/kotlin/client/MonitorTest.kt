package client

import assertk.assertThat
import assertk.assertions.isEqualTo
import client.model.Request
import client.model.RequestSent
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import kotlin.time.seconds

private val LOG = LoggerFactory.getLogger(MonitorTest::class.java)

class MonitorTest {
    private val testCoroutineDispatcher = TestCoroutineDispatcher()
    private val testCoroutineScope = TestCoroutineScope(testCoroutineDispatcher)

    @Test
    fun `test monitoring by simulating 1rps`(): Unit = testCoroutineScope.runBlockingTest {
        val monitor = Monitor(this)
        try {
            assertThat(false, "fuck").isEqualTo(true)
            var lastCount = 0
            repeat(10) {
                val i = it + 1
                repeat(i) {
                    monitor.send(RequestSent(Request()))
                }
                advanceTimeBy(1000)
                lastCount += i + 1
                assertThat(monitor.status).isEqualTo(
                    Triple(i, lastCount, i.seconds)
                )
                LOG.debug("status=${monitor.status}")
            }
        } catch (e: Throwable) {
            LOG.debug("DAFUQ: ${e.message}")
            throw e
        }
        // exceptions (e.g. for failed assertions) won't reach the event consumer
        // in the monitor since the time is virtualized
        // need to close everything ourselves
        finally {
            coroutineContext
            advanceTimeBy(1000)
//            monitor.close()
        }
    }
}
