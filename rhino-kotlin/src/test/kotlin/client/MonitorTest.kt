package client

import client.model.Request
import client.model.RequestSent
import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

private val LOG = LoggerFactory.getLogger(MonitorTest::class.java)

class MonitorTest {
    @Test
    fun `test monitoring by simulating 1rps`(): Unit = runBlocking {
        withTimeout(10000) {
            val monitor = Monitor(CoroutineScope(Dispatchers.IO))
            var lastCount = 0
            repeat(10) {
                val i = it + 1
                repeat(i) {
                    monitor.send(RequestSent(Request()))
                }
//                assertThat(monitor.status).isEqualTo(
//                    Triple(i, lastCount, it.seconds)
//                )
                delay(1000)
                LOG.debug("status=${monitor.status}")
            }
//        // exceptions (e.g. for failed assertions) won't reach the event consumer
//        // in the monitor since the time is virtualized
//        // need to close evetruerything ourselves
        }
    }
}
