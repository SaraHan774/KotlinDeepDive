package coroutine_deepdive

import kotlinx.coroutines.delay
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun main() {
    println("before")
    val user = requestUser()
    println(user)
    println("after")
}

private suspend fun requestUser(): String {
    return suspendCoroutine { continuation ->
        // 일반 함수를 실행할 수 있다
        requestUser { user ->
            continuation.resume(user)
        }
    }
}

private fun requestUser(onSuccess: (String) -> Unit) {
    onSuccess("Hello World")
}

//private val executor = Executors.newSingleThreadScheduledExecutor {
//    Thread(it).apply { isDaemon = true }
//}
//
//private suspend fun delay(timeMillis: Long): Unit = suspendCoroutine { continuation ->
//    executor.schedule({continuation.resume(Unit)}, timeMillis, TimeUnit.MILLISECONDS)
//}
//
//suspend fun main() {
//    println("before") // 1
//    delay(1000)
//    println("after") // 3
//}

//suspend fun main() {
//    println("before") // 1
//    suspendCoroutine<Unit> { continuation ->
//        println("${continuation.context}")
//        println("before before") // 2
//        continuation.resume(Unit) // 실행이 재개된다
//        println("after resume") // 3
//        continuation.resume(Unit) // throws exception - Already Resumed ...
//    }
//    println("after")
//}