package client

import Client
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.lang.Integer.min
import kotlin.time.seconds

private val LOG = LoggerFactory.getLogger(ClientMonitorTest::class.java)

class ClientMonitorTest {
    @Test
    fun `test client+monitor integration`() = runBlocking<Unit> {
        val simDuration = 20.seconds
        val rampUpDuration = 10.seconds
        val parallelScenarios = 1000

        withTimeout(simDuration) {
            val client = Client(this) {
                rateLimit {
                    startRps = 100
                    targetRps = 200
                    timeSpan = rampUpDuration
                }
            }
            repeat(parallelScenarios) {
                launch(CoroutineName("scenario") + Dispatchers.Default) {
                    while (coroutineContext.isActive) {
                        client.url("http://localhost:8080/foo").get()
                    }
                }
            }

            var expectedCount = 0
            repeat(simDuration.minus(2.seconds).inSeconds.toInt()) {
                //delay a bit more than a seconds to make it more likely that there is a status update
                delay(1050)
                var rps = 100 + it * 10
                rps = min(rps, 200)
                val (actualRps, actualTotalRequests, actualClock) = client.monitor.status
                expectedCount += rps
                assertThat(actualRps).isEqualTo(rps)
                assertThat(actualTotalRequests).isEqualTo(expectedCount)
                assertThat(actualClock).isEqualTo(it.seconds + 1.seconds)
            }
            client.close()
        }
    }
}