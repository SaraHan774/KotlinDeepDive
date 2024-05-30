package coroutine_deepdive

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// Default Dispatcher - CPU core

suspend fun main() {
    dispatcher2()
}


suspend fun dispatcher1() = coroutineScope {
    repeat(1000) {
        launch {
            List(1000) { Random.nextLong() }.maxOrNull() // busy CPU
            val threadName = Thread.currentThread().name
            println("Running $threadName") // 10 cores ?
        }
    }
}

suspend fun dispatcher2() = coroutineScope {
    repeat(1000) {
        launch(Dispatchers.IO) {
            delay(1000)
            val threadName = Thread.currentThread().name
            println("Running $threadName") // 68 까지 보이는 듯 ?
        }
    }
}

