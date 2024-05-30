import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

suspend fun doSomethingUsefulOne(): Int {
    delay(1000L) // simulate some computation
    return 13
}

suspend fun doSomethingUsefulTwo(): Int {
    delay(1000L) // simulate some computation
    return 29
}

fun main() = runBlocking {
    val time = measureTimeMillis {
        // Lazy 안넣으면 동시에 시작함 -> 다 해서 1초 걸림
        // Lazy 넣으면 순차적으로 시작됨 (await 호출한 순서대로 시작함)
        val one = async(start = CoroutineStart.LAZY) { doSomethingUsefulOne() }
        val two = async(start = CoroutineStart.LAZY) { doSomethingUsefulTwo() }

        // some computation

        // Lazy 사용하는 경우 명시적으로 start 를 호출해 주어야 함
//        one.start() // start the first one
//        two.start() // start the second one
        println("The answer is ${one.await() + two.await()}")
    }
    println("Completed in $time ms")
}
