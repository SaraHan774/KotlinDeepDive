package coroutine_deepdive

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        val offers = getOffers(
            listOf(
                "seller 1",
                "seller 2",
                "seller 3",
                "seller 4",
                "seller 5",
                "seller 6",
                "seller 7",
            )
        )
        println(offers)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun getOffers(
    sellers: List<String>
) :List<String> = sellers.asFlow()
    .flatMapMerge(concurrency = 20) { seller ->
        suspend { requestOffer(seller) }.asFlow()
    }.toList()

private suspend fun requestOffer(seller: String): String {
    delay(100)
    return "OFFER OF $seller"
}

//private fun flatMapTest() {
//    val startTime = System.currentTimeMillis()
//
//    (1..3).asFlow()
//        .onEach { delay(100) }
//        .flatMapMerge { requestFlow(it) }
//        .collect { value ->
//            println("$value at ${System.currentTimeMillis() - startTime} ms from start")
//        }
//}
//
//fun requestFlow(i: Int): Flow<String> = flow {
//    emit("$i: First")
//    delay(500) // wait 500 ms
//    emit("$i: Second")
//}