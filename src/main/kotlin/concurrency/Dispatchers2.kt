package concurrency

import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.milliseconds

fun main() {
    val numOption : Int = runCatching {  readln().toInt() }.getOrElse { 0 }
    when(numOption) {
        0 -> {
            // 예제 1: 기본적인 UNDISPATCHED 동작 이해
            runBlocking {
                println("1 ${Thread.currentThread().name}") // 메인 스레드에서 실행
                launch(Dispatchers.Default, start = CoroutineStart.UNDISPATCHED) {
                    // UNDISPATCHED 덕분에 현재 스레드에서 바로 실행됨
                    println("2 ${Thread.currentThread().name}")
                    delay(10.milliseconds) // 첫 중단점 (suspension point)
                    // 중단점 이후에는 Dispatchers.Default 스레드에서 실행됨
                    println("4 ${Thread.currentThread().name}")
                }
                // launch 블록이 시작된 후 메인 스레드에서 계속 실행
                println("3 ${Thread.currentThread().name}")
            }
        }

        1 -> {
            // 예제 2: UNDISPATCHED와 재귀 호출
            runBlocking {
                println("예제 2: 재귀 호출 테스트 시작")

                // Dispatchers.Unconfined 사용
                val factorialUnconfined = factorialWithUnconfined(5)
                println("결과 (Unconfined): ${factorialUnconfined.await()}")

                // CoroutineStart.UNDISPATCHED 사용
                val factorialUndispatched = factorialWithUndispatched(5)
                println("결과 (UNDISPATCHED): ${factorialUndispatched.await()}")
            }
        }

        2 -> {
            // 예제 3: 취소된 상태에서도 UNDISPATCHED 실행
            runBlocking {
                println("1. 스코프를 취소합니다.")
                cancel() // 현재 스코프를 취소

                println("2. UNDISPATCHED로 코루틴을 시작합니다.")
                launch(start = CoroutineStart.UNDISPATCHED) {
                    // 이미 취소된 상태에서도 실행이 시작됨
                    println("3. 취소 상태에서 코루틴 진입")
                    check(!isActive) // isActive는 false 상태
                }
                println("4. 외부 코루틴이 계속 실행됩니다.")
            }
        }

        3 -> {
            runBlocking {
                launch(start = CoroutineStart.UNDISPATCHED) { performTask("task1") }

                launch(start = CoroutineStart.ATOMIC) { performTask("task2") }

                // 이렇게 하지 않으면 시작 하지 않고 Hanging 상태가 된다.
                val job = launch(start = CoroutineStart.LAZY) { performTask("task3") }
                job.start()
                launch(start = CoroutineStart.DEFAULT) { performTask("task4") }
            }
        }

        4 -> {
            runBlocking {
                launch(Dispatchers.Default) { println("HelloWorld") } // -> 뒤늦게 스케줄링 되어서 시작함
                println("EndOfTheWorld") // -> 먼저 시작함
                // 코루틴 런칭 할 경우 실행 순서 보장이 안되는 문제가 있음

                //2. 특정 요구사항
                //일부 시나리오에서는 코루틴의 실행이 즉시 시작되고, 첫 번째 중단점까지 반드시 완료되어야 하는 경우가 있습니다.
                //	•	초기화 보장: 예를 들어, 이벤트 리스너나 구독 작업을 등록하는 경우, 반드시 등록이 완료된 상태에서 이후의 작업이 실행되어야 합니다.
                //	•	중단점 이전 동작 보장: 코루틴의 첫 번째 작업이 꼭 실행되도록 보장하고 싶을 때.

                // 기존의 디스패처 기반 스케줄링 방식은 스케줄링 지연과 함께 취소 상태에서도 작업이 시작되지 않을 가능성이 있었습니다.
                // CoroutineStart.UNDISPATCHED는 이러한 요구사항을 충족시키기 위해 도입되었습니다.

                // CoroutineStart.UNDISPATCHED는 현재 호출된 스레드에서 즉시 코루틴을 실행하고, 중단점 이후에는 디스패처의 스케줄링을 따르도록 설계되었습니다.

                // 주요 특징
                // •	즉시 실행: 코루틴은 현재 호출 스레드에서 첫 번째 중단점까지 실행됩니다.
                // •	취소 무시: 코루틴이 이미 취소된 상태여도 첫 번째 중단점 이전까지는 실행됩니다.
                // •	스케줄링 최소화: 스케줄링 오버헤드를 줄이고, 실행이 보장됩니다.

                // 리스너를 등록하는 경우
                launch(start = CoroutineStart.UNDISPATCHED) {
                    println("Register event listener") // 먼저 실행
                    delay(100)
                    println("After delay 100") // 마지막으로 실행
                }
                println("Main Coroutine") // 두번째로 실행
            }
        }

        5 -> {
            runBlocking {
                cancel()
                launch(start = CoroutineStart.UNDISPATCHED) {
                    println("After cancel")
                    delay(100) // 이하 실행 안되고 캔슬 되어서 JobCancellationException 발생
                    println("After delay")
                }
            }
        }
    }
}

// UNDISPATCHED를 사용한 팩토리얼 계산
fun CoroutineScope.factorialWithUndispatched(n: Int): Deferred<Int> =
    async(start = CoroutineStart.UNDISPATCHED) {
        if (n > 0) {
            n * factorialWithUndispatched(n - 1).await() // 재귀 호출
        } else {
            1
        }
    }

// Dispatchers.Unconfined를 사용한 팩토리얼 계산
fun CoroutineScope.factorialWithUnconfined(n: Int): Deferred<Int> =
    async(Dispatchers.Unconfined) {
        if (n > 0) {
            n * factorialWithUnconfined(n - 1).await() // 재귀 호출
        } else {
            1
        }
    }

suspend fun performTask(taskName: String) {
    println("Starting $taskName on thread: ${Thread.currentThread().name}")
    delay(500) // 첫 번째 중단지점
    println("Halfway through $taskName on thread: ${Thread.currentThread().name}")
    delay(500) // 두 번째 중단지점
    println("Finished $taskName on thread: ${Thread.currentThread().name}")
}
