import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import java.time.Instant
import java.util.concurrent.CompletableFuture

fun log(msg: String) = println("${Instant.now()} [${Thread.currentThread().name}] $msg")

// add coroutineScope so that all coroutines in this method are managed (e.g. canceled on an exception)
// coroutineScope is a suspending function which suspends until all inner suspendable functions finished
suspend fun processA(): Double = coroutineScope {
    log("processing...")
    delay(1000)
    log("processA completed")
    Math.random()
}

suspend fun processB(): Double = coroutineScope {
    log("throwing up")
    delay(500)
    throw RuntimeException("Something terrible happened")
}

fun legacyProcessA(): CompletableFuture<Int> {
    log("processing legacy...")
//    runs on commonPool
    return CompletableFuture.supplyAsync {
        Thread.sleep(400)
        log("legacyProcessA completed first stage")
        42;
    }.thenApplyAsync { // this will be canceled
        Thread.sleep(300)
        log("legacyProcessA did something else too...")
        it
    }
}


// runBlocking runs coroutines in the main thread
fun main() = runBlocking<Unit> {
    // coroutine builders inherit dispatcher from parent
    // which means launch() without Dispatchers.Default would run all
    // coroutines on a single thread (no parallelization possible)
    // Dispatchers.Default is like ForkeJoin.commonPool and meant for CPU intensive tasks
    // note: launch returns immediately
    val job = launch(Dispatchers.Default) {
        var res3: CompletableFuture<Int>? = null
        try {
            val res0 = async { processA() } // without async it would suspend
            val res1 = async { processA() }
            // async's return type (CompletableDeferred), can be constructed manually too
            val res2 = CompletableDeferred(123)
            res3 = legacyProcessA() // is not managed so take care
            processB() // throw exception
            awaitAll(res0, res1, res2)
            res3.await()
        } finally {
            // btw completable futures cancellation doesn't interrupt
            log("Cancelling futures...")
            res3?.cancel(true)
        }
    }
    log("most likely this will be printed first")
}