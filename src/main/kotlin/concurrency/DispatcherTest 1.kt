package concurrency

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        launch(Dispatchers.Unconfined) {
            println("${Thread.currentThread().name}") // main
            delay(1000)
            println("${Thread.currentThread().name}") // DefaultExecutor
        }
    }
}