package coroutine_deepdive

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.random.Random

// Default Dispatcher - CPU core

suspend fun main() = coroutineScope {
    repeat(1000) {
        launch {
            List(1000) { Random.nextLong() }.maxOrNull() // busy CPU
            val threadName = Thread.currentThread().name
            println("Running $threadName")
        }
    }
}