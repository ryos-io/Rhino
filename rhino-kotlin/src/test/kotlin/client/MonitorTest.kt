package client

import assertk.assertThat
import assertk.assertions.isEqualTo
import client.model.Request
import client.model.RequestSent
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import kotlin.time.milliseconds

private val LOG = LoggerFactory.getLogger(MonitorTest::class.java)

class MonitorTest {
    @Test
    fun `test monitoring by simulating 1rps`(): Unit = runBlocking {
        // init logger otherwise logging may be broken due to parallel calls
        LOG.debug("started...")
        withTimeout(1500) {
            val interval = 100.milliseconds
            val monitor = Monitor(this, interval = interval)
            var lastCount = 0
            repeat(10) {
                val i = it + 1
                repeat(i) {
                    monitor.send(RequestSent(Request()))
                }
                val expectedCount = lastCount + i
                assertThat(monitor.status).isEqualTo(
                    Status(it * 10, lastCount, (it * interval.inMilliseconds).milliseconds)
                )
                lastCount = expectedCount
                delay(110) // give it a bit time to calculate
            }
            monitor.close()
        }
    }
}
