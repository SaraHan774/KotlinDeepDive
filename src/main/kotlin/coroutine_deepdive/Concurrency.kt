package coroutine_deepdive

import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger

var counter = AtomicInteger(0)
var job: Job? = null

fun main() = runBlocking {
    massiveRun2 { index, scope ->
        testing(index >= 1, scope)
    }
    println("Counter: $counter")
}

suspend fun massiveRun2(block: suspend (Int, CoroutineScope) -> Unit) {
    withContext(Dispatchers.IO) {
        repeat(100) { i -> block(i, this) }
    }
}

private suspend fun testing(isRefresh: Boolean, scope: CoroutineScope) {
    if (isRefresh) {
        job?.cancel()
        job = null
    }

    if (job == null || job?.isActive == false) {
        job = scope.launch {
            // Simulate some work that might throw an exception
            println("start")
            delay(1000)
            counter.getAndIncrement()
            println("finish")
        }
        job?.invokeOnCompletion {
            job = null // Reset job after completion or failure
        }
    }
}
