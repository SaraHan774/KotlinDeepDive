package concurrency

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


fun main() = runBlocking {
    withContext(counterContext) {
        massiveRun { //100개의 코루틴을 만들고, 각 코루틴마다 1000번 반복 증가
            // 100개의 코루틴 안에서 각 작업을 한 스레드에 넣는것과 달리
            // 100개의 코루틴을 생성하는 작업까지 하나의 스레드로 처리하면
            // 좀 더 coarse 한 스레드 컨트롤이 되지만 속도는 빨라진다
            counter++
        }
    }
    println("Counter = $counter")
}