import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test

class CoroutineTestDemo {
    private val testCoroutineDispatcher = TestCoroutineDispatcher()
    private val testCoroutineScope = TestCoroutineScope(testCoroutineDispatcher)

    // you can also run `runBlockingTest` on itself
    // which would create an instance of TestCoroutineScope implicitly
    // when you create it yourself you can pass it to the unit under test
    // to prevent them from running in another scope and ignoring the virtual time
    @Test
    fun `shows how TestCoroutineScope works`(): Unit = testCoroutineScope.runBlockingTest {
        val list = mutableListOf<Int>()
        launch {
            list.add(calc42())
            list.add(calc42())
        }
        assertThat(list).isEmpty()
        advanceTimeBy(1000)
        assertThat(list).hasSize(1)
        advanceTimeBy(1000)
        assertThat(list).hasSize(2)
    }


    @Test
    fun `shows how to launch coroutines in the background`(): Unit =
        testCoroutineScope.runBlockingTest {
            val res = mutableListOf<Deferred<Int>>()
            launch {
                // we don't need to advance time since we deferred
                repeat(2) {
                    res.add(async { calc42() })
                }
            }
            assertThat(res).hasSize(2)
            advanceTimeBy(500)
            assertThat(!res.all { it.isCompleted }, "should not be completed")
            advanceTimeBy(500)
            assertThat(res.all { it.isCompleted }, "should be completed")
        }

    // coroutineScope inherits context from parent
    suspend fun calc42(): Int = coroutineScope {
        delay(1000)
        42
    }
}