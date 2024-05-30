package coroutine_deepdive

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.system.measureTimeMillis

class MessagesRepository {
    private val messages = mutableListOf<String>()
    private val mutex = Mutex()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher = Dispatchers.IO.limitedParallelism(10)

    suspend fun add(message: String) {
        // 버전1. Single thread 로 제한된 디스패처 사용 - 각자 런치 되었다가 병렬 실행후 돌아오는 느낌 (독립적) - 그래서 총 1초 정도 걸림.
        withContext(dispatcher) {
            delay(1000)
            println(message)
            messages.add(message)
        }

        // 버전2. Mutex 사용
        mutex.withLock { // queueing 된다. 코루틴들이 줄서서 줄줄이 소시지마냥 기다린다. 그래서 총 5초 이상 걸린다.
            delay(1000)
            println(message)
            messages.add(message)
        }
    }
}

suspend fun main() = runBlocking {
    val repo = MessagesRepository()
    val timeMills = measureTimeMillis {
        coroutineScope {
            repeat(5) {
                launch {
                    repo.add("Hello $it")
                }
            }
        }
    }
    println("time $timeMills ms")
}