package concurrency

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger

fun main() = runBlocking {
    val atomicCounter = AtomicInteger(0)
    withContext(Dispatchers.Default) {
        massiveRun {
            atomicCounter.getAndIncrement() // 17ms
        }
    }
    println("Counter (atomic) = $atomicCounter")
}