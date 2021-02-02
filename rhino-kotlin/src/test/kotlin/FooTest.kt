import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test

class FooTest {
    @ExperimentalCoroutinesApi
    @Test
    fun testDelay() = runBlockingTest {
        val data = mutableListOf<Int>()
        launch {
            genTestData().toList(data)
        }
        assertThat(data).isEmpty()
        advanceTimeBy(1000)
        assertThat(data).containsExactly(1)
        advanceTimeBy(1000)
        assertThat(data).containsExactly(1,2)
    }

    suspend fun genTestData() = flow {
        (1..10).forEach {
            delay(1000)
            emit(it)
        }
    }
}