import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

val LOG = LoggerFactory.getLogger("Playground")

runBlocking {
    val client = Client(this)
    client.use {
        launch {
            while (true) {
                it.url("httadadssas").get()
                delay(100)
            }
        }
    }
    delay(500)
    throw IllegalStateException("i throw something")
}
