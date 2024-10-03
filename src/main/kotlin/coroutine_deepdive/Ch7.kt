package coroutine_deepdive

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

fun main() {
    val context = CoroutineName("Coroutine")
    val coroutineName = context[CoroutineName]
    println(coroutineName?.name) // Coroutine
    val job = context[Job]
    println(job) // null

    val me = context[CoroutineNameMe]
    println(me?.name)
}

data class CoroutineNameMe(val name: String) : AbstractCoroutineContextElement(CoroutineName) {
    override fun toString(): String {
        return super.toString()
    }

    companion object Key: CoroutineContext.Key<CoroutineNameMe>
}

//fun main() {
//    val name: CoroutineName = CoroutineName("Coroutine")
//    val element: CoroutineContext.Element = name
//    val context: CoroutineContext = element // 컨택스트 내 요소도 컨택스트
//
//    val job: Job = Job()
//    val jobElement: CoroutineContext.Element = job
//    val jobContext: CoroutineContext = jobElement // job 도 컨택스트
//}
