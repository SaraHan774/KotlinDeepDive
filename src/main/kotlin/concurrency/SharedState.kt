package concurrency

import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

var counter = 0

@Volatile // 전혀 도움이 안되는데
var counterV = 0

// counting 하는 것은 이 컨택스트에만 confine 한다.
// fine grained control over thread 를 하고자 한다
// Confine => 하나의 스레드에 counter 작업을 묶어두고자 한다

val counterContext = newSingleThreadContext("CounterContext")

fun main() = runBlocking {
    withContext(Dispatchers.Default) {
        massiveRun {
            // massive run 안의 action 블록은 1000번 실행되는데
            // 실행 될 때 마다 counterContext 에서 실행
            // Dispatchers.Default 는 멀티 스레디드 환경
            // Single Threaded 인 컨택스트 이므로 thread - safe 하다
            withContext(counterContext) {
                counter++
            }
        }
    }
    println("Counter = $counter")

    // 그렇다면 위 작업을 통째로 singled threaded 로 ?
}

// 변수를 볼래틸로 선언하면 괜찮을까?




// 100 개의 코루틴을 실행
// 각 코루틴마다 action 블록을 1000번 식행
// 이론적으로 100,000번 카운터가 증가해야 함
suspend fun massiveRun(action: suspend () -> Unit) {
    val n = 100  // number of coroutines to launch
    val k = 1000 // times an action is repeated by each coroutine
    val time = measureTimeMillis {
        coroutineScope { // scope for coroutines
            repeat(n) {
                launch {
                    repeat(k) { action() }
                }
            }
        }
    }
    println("Completed ${n * k} actions in $time ms")
}

