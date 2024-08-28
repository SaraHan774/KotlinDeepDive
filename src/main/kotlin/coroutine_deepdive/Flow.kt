package coroutine_deepdive

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext

//fun main() {
//    sequence<Int> {
//        yield(1)
//        yield(2)
//    }
//    val fibonacciSequence = object : Sequence<Int> {
//        override fun iterator(): Iterator<Int> {
//            return object : Iterator<Int> {
//                var a = 0
//                var b = 1
//
//                override fun hasNext(): Boolean {
//                    return true // Infinite sequence
//                }
//
//                override fun next(): Int {
//                    val nextValue = a
//                    a = b
//                    b += nextValue
//                    return nextValue
//                }
//            }
//        }
//    }
//
//    // Take the first 10 Fibonacci numbers and print them
//    fibonacciSequence.take(10).forEach { println(it) }
//}

//fun getSequence(): Sequence<String> = sequence {
//    repeat(3) {
//        Thread.sleep(1000)
//        yield("User$it")
//    }
//}
//
//suspend fun main() {
//    withContext(newSingleThreadContext("main")) {
//        launch {
//            repeat(3) {
//                delay(100)
//                println("Processing on coroutine")
//            }
//        }
//
//        val list = getSequence()
//        list.forEach { println(it) }
//    }
//}

fun getFlow(): Flow<String> = flow {
    repeat(3) {
        delay(1000)
        emit("User$it")
    }
}

suspend fun main() {
    withContext(newSingleThreadContext("main")) {
        launch {
            repeat(3) {
                delay(100)
                println("Processing on coroutine")
            }
        }
        val list = getFlow()
        list.collect { println(it) } // non - blocking
    }
}