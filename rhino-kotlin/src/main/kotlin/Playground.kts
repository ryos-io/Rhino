import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.time.Instant

fun log(msg: String) = println("${Instant.now()}[${Thread.currentThread().name}] $msg")

suspend fun firstName(): String {
    log("processing firstName")
    delay(5000)
//    while (true) {
//        log("won't give up, fuck you")
//        Thread.sleep(200)
//    }
    log("end firstName")
    return "Bianca"
}

suspend fun lastName(): String {
    log("processing lastName")
    delay(1000)
    log("end lastName")
    return "Noack"
}

// single thread by default
runBlocking {
    val firstName: Deferred<String> = async { firstName() }
    val lastName: Deferred<String> = async { lastName() }

    // we need now the values so thread will block
    log("starting greeting")
//    while (true) {
//        Thread.sleep(200)
//        log("Laber nicht!")
//    }
    log("hello ${firstName.await()} ${lastName.await()}")
    log("bye")
}
