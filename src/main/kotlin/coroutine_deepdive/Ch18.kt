package coroutine_deepdive

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    flowTest()
}

// sequece test
private fun sequenceVSList() {
    fun m(i: Int) : Int {
        print("m$i ")
        return i * i
    }

    fun f(i: Int) : Boolean {
        print("f$i ")
        return i >= 10
    }

    listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        .map { m(it) }
        .find { f(it) }
        .let { println(it) }

    sequenceOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        .map { m(it) }
        .find { f(it) }
        .let { println(it) }
}

private suspend fun channelTest() {
    fun CoroutineScope.makeChannel() = produce {
        println("Channel started")
        for (i in 1..3) {
            kotlinx.coroutines.delay(1000)
            send(i)
        }
    }

    coroutineScope { // 코루틴 런칭
        val channel = makeChannel()
        delay(1000)
        println("Calling channel ... ")
        for (value in channel) {
            println(value)
        }
        // 여기서는 값이 나오지 않을 것이다 -> 채널의 값은 한번만 방출된다
        println("Consuming again ...")
        for (value in channel) {
            println(value)
        }
    }
}

private suspend fun flowTest() {
    fun makeFlow() = flow {
        println("Flow Started")
        for (i in 1..3) {
            delay(1000)
            emit(i)
        }
    }

    val flow = makeFlow() // 미리 정의만 해둔다. 코루틴 런칭하는 것 필요 없음
    delay(1000)
    println("Calling flow...")
    flow.collect { value -> println(value) } // 이 시점에 생성
    println("Consuming again ...")
    flow.collect { value -> println(value) }
}