import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

val LOG = LoggerFactory.getLogger("Playground")

//runBlocking {
//    val client = Client(this)
//    client.use {
//        launch {
//            while (isActive) {
//                it.url("httadadssas").get()
//                delay(100)
//            }
//        }
//    }
//    delay(500)
//    throw IllegalStateException("i throw something")
//}

//runBlocking {
//    // cancels with other context
//    val a = actor<Int>(capacity = 1000) {
//        try {
//            while (isActive) {
//                consumeEach {
//                    LOG.debug("got something $it")
//                }
//                delay(10)
//            }
//        } catch (e: Throwable) {
//            LOG.error("Got error: ${e.message}")
//            throw e
//        }
//    }
//    a.send(1)
//    a.send(3)
//    delay(100)
//    throw IllegalStateException("OH SHIT")
//}


runBlocking {
    launch {
        while (isActive) {
            delay(100)
            LOG.debug("hello world")
        }
    }
    delay(500)
    throw IllegalStateException("fuck off")
}