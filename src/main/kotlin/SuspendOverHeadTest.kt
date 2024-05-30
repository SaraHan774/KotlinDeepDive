import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

fun normalFunction(a: Int, b: Int): Int {
    return a + b
}

suspend fun suspendFunction(a: Int, b: Int): Int {
    return a + b
}

fun main() {
    val iterations = 1_000_000

    // Benchmark normal function
    val normalTime = measureTimeMillis {
        repeat(iterations) {
            normalFunction(1, 2)
        }
    }

    // Benchmark suspend function
    val suspendTime = measureTimeMillis {
        runBlocking {
            repeat(iterations) {
                suspendFunction(1, 2)
            }
        }
    }

    println("Normal function time: $normalTime ms")
    println("Suspend function time: $suspendTime ms")
}