package coroutine_deepdive

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    with(CustomFlowOperators()) {
        testCustom()
    }
}

class CustomFlowOperators {

    //Your lucky number today is 48
    //[1] Good bye
    //Your lucky number today is 3
    //[2] Good bye
    //Your lucky number today is 25
    //[3] Good bye
    //Your lucky number today is 20
    //[4] Good bye

    fun <T> Flow<T>.makeFlowFunky() : Flow<T> = flow {
         // val random = Math.random() --> 여기에 선언하면 매번 같은 값이 나온다
        collect {
            println("Your lucky number today is ${(Math.random() * 100).toInt()}")
            emit(it)
        }
    }

    suspend fun <T> Flow<T>.sayGoodbye(): T {
        var ret: T? = null
        collect {
            println("[$it] Good bye")
            ret = it
        }
        if (ret == null) throw NoSuchElementException("Nulls go to heaven.")
        @Suppress("UNCHECKED_CAST")
        return ret as T
    }

    suspend fun testCustom() {
        flowOf(1, 2, 3, 4)
            .makeFlowFunky()
            .sayGoodbye()
    }
}

private suspend fun testTerminal() {
    val flow = flowOf(1, 2, 3, 4).map { it * it }
    println(flow.first())
    println(flow.last())
    println(flow.count())

    println(flow.reduce { accumulator, value -> accumulator * value })
    println(flow.fold(0) {accumulator, value -> accumulator + value})
}

private suspend fun testDistinct() {
    flowOf(1, 2, 3, 4, 5, 5, 5, 5)
        .distinctUntilChanged()
        .collect { value -> println(value) }
}

private class Distinct {

    fun <T> Flow<T>.distinctUntilChanged(): Flow<T> = flow {
        var previous: Any? = NOT_SET
        this@distinctUntilChanged.collect (collector = FlowCollector { // 이하는 emit 함수에 대한 정의
            if (previous == NOT_SET || previous != it) {
                this@flow.emit(it)
                previous = it
            }
        })
    }
    private val NOT_SET = Any()
}

private class Retry {

    fun <T> Flow<T>.retryWhen(
        predicate: suspend FlowCollector<T>.(cause: Throwable, attempt: Long) -> Boolean
    // N 회의 재시도 찬스를 주고 만약 predicate 에서 재시도 하지 않음으로 false 를 리턴하면 throw e 한다
    ): Flow<T> = flow {
            var attempt = 0L
            do {
                val shallRetry = try {
                    collect { emit(it) } // emit(it) 하는 순간 while 구문은 종료되고, 정상적으로 값이 방출된다.
                    false
                } catch (e: Throwable) {
                    predicate(e, attempt++).also { if (!it) throw e }
                }
            } while (shallRetry)
        }

    fun <T> Flow<T>.retry(
        retries: Long = Long.MAX_VALUE,
        predicate: suspend (cause: Throwable) -> Boolean = { true }
        // 오류가 발생했을 때 행할 것 그리고, Boolean 은 retry 진행 여부
    ) : Flow<T> {
        require(retries > 0) {
            "Expected positive amount of retries, but had $retries"
        }
        return retryWhen { cause, attempt ->
            attempt < retries && predicate(cause)
        }
    }
}

private suspend fun testFlat() {
    // flatMap
    // flatMap = map & flatten
    // 플로우에서 flatMap 은 어떻게 봐야 할까요?
    println("=== flatMapLatest ===")
    flowOf("A", "B", "C")
        .flatMapLatest { flowFrom(it) } // concat 이랑 비슷한데, delay 있는 상태에서 다음 요소 들어오면 이전 요청 취소됨
        .collect(::println)

    println("=== flatMapConcat ===")
    flowOf("A", "B", "C") // 두 플로우가 concat 될 수 있을때까지, 쌍을 이룰수 있을때까지 기다린다
        .flatMapConcat { flowFrom(it) }
        .collect(::println)

    println("=== flatMapMerge ===") // Merge 해서 방출한다 (각 플로우 두개가 말그대로 합쳐진다)
    flowOf("A", "B", "C", "D", "E", "F")
        .flatMapMerge(concurrency = 3) { flowFrom(it) } // concurrency 옵션 있어서, 동시에 기본으로 16개까지 처리 가능
        .collect(::println)
}

private fun flowFrom(element: String) = flowOf(1, 2, 3, 4, 5, 6)
    .onEach { delay(1000) }
    .map { "${it}_$element" }

private suspend fun testFoldScan() {
    val list = listOf(1, 2, 3, 4)
    val res = list.fold(0) { acc, i -> acc + i }
    println(res)
    val res2 = list.fold(1) { acc, i -> acc * i }
    println(res2)

    val res3 = list.scan(1) { acc, i -> acc * i } // returns a list!
    // list 안에는 중간 계산 결과들이 모두 담겨있다.
    // [1, 1, 2, 6, 24]
    println(res3)

    val res4 = list.scan(2) { acc, i -> acc * i }
    // 첫번째 원소는 항상 초기값이다.
    println(res4)
    // 스캔은 이전 단계에서 값을 받은 즉시 새로운 값을 만들기 떄문에 플로우에서 유용하게 사용된다

    flowOf(1, 2, 3, 4)
        .onEach { delay(1000) }
        .scan(0) { acc, i -> acc + i } // fold 와 달리 최종 연산자가 아니라, 중간 연산자로서 리스트 생성함
        .collect { println(it) } // 누산 값을 방출함.
}

private class ScanImplementation() {
    fun <T, R> Flow<T>.scan(
        initial: R,
        operation: suspend (accumulator: R, value: T) -> R // 누산 연산은 중단함수일 수 있다
    ) = flow { // flow builder
        var accumulator: R = initial
        emit(accumulator) // 초기값을 방출한 후에
        collect { value ->
            accumulator = operation(accumulator, value) // 누산기에 연산한 결과를 할당하고 방출한다
            emit(accumulator)
        }
    }
}


data class Book(
    val title: String,
    val authors: List<String>,
)

private fun test() {
    val books = listOf(
        Book("Harry Potter", listOf("George Orwell")),
        Book("Harry Potter2", listOf("George Orwell2")),
    )

    val result = books.map { it.authors }.flatten()
    println(result)

    val result1 = books.flatMap { it.authors }
    println(result1)
    // [George Orwell, George Orwell2]
    // 둘 다 동일하게 위 결과를 반환한다.
}

private fun test1() {
    // atomic kotlin 283 pg
    val intRange = 1..3
    val result = intRange.map { a ->
        intRange.map { b -> a to b }
    }
    println("map & map")
    println(result)
    //[[(1, 1), (1, 2), (1, 3)], [(2, 1), (2, 2), (2, 3)], [(3, 1), (3, 2), (3, 3)]]

    val result1 = intRange.map { a ->
        intRange.map { b -> a to b }
    }.flatten()
    println("map & flatten")
    println(result1)
    //[(1, 1), (1, 2), (1, 3), (2, 1), (2, 2), (2, 3), (3, 1), (3, 2), (3, 3)]

    val result2 = intRange.flatMap { a ->
        intRange.map { b -> a to b }
    }
    println("flatMap")
    println(result2)
    //[(1, 1), (1, 2), (1, 3), (2, 1), (2, 2), (2, 3), (3, 1), (3, 2), (3, 3)]
}