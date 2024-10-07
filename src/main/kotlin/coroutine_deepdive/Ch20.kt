package coroutine_deepdive

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    testFlowCounter()
}

private fun understandFlow() {
    val f: () -> Unit = {
        print("A")
        print("B")
        print("C")
    }

    f() // ABC
    f() // ABC
}

private suspend fun understandFlow2() {
    val f: suspend () -> Unit = {
        print("A")
        delay(100)
        print("B")
        delay(100)
        print("C")
    }

    f()
    f()
}

private suspend fun understandFlow3() {
    val f: suspend ((String) -> Unit) -> Unit = { emit ->
        emit("A")
        emit("B")
        emit("C")
    }

    // function block 을 인자로 받는다 - 인자의 이름은 emit 이다.
    f {
        print(it) // ABC
    }

    f {
        print(it) // ABC
    }
}

// emit 이라는 함수를 감싼 "함수형 인터페이스"
// 함수를 감싼 함수형님? ㅋ
private fun interface FlowCollector2 {
    suspend fun emit(value: String)
}

private suspend fun understandFlow4_1() {
    val f: suspend (FlowCollector2) -> Unit = {
        it.emit("A")
        it.emit("B")
        it.emit("C") // it 계속 반복 안하려면 FlowCollector 를 리시버로 받으면 됨
    }
    f { print(it) }
    f { print(it) }
}

private suspend fun understandFlow4_2() {
    val f: suspend FlowCollector2.() -> Unit = {
        emit("A")
        emit("B")
        emit("C")
    }

    f { print(it) }
    f { print(it) }
}

private interface Flow2 {
    // FlowCollector 를 파라미터로 받는 람다 자체를 인터페이스로 추상화
    suspend fun collect(collector: FlowCollector2)
}

private suspend fun understandFlow5() {
    // 아래 두 객체들을 플로우 빌더로 추상화
    val builder: suspend FlowCollector2.() -> Unit = {
        emit("A")
        emit("A")
        emit("A")
    }

    val flow: Flow2 = object : Flow2 {
        override suspend fun collect(collector: FlowCollector2) {
            collector.builder()
        }
    }

    flow.collect { print(it) }
    flow.collect { print(it) }
}

// 콜렉터에 대한 확장 함수를 인자에서 정의한다
private fun flow2(builder: suspend FlowCollector2.() -> Unit) = object : Flow2 {
    override suspend fun collect(collector: FlowCollector2) {
        collector.builder() // 인자에서 정의된 빌더를 콜렉터에 호출
    }
}

private suspend fun understandFlow6() {
    val flow = flow2 {
        emit("A")
        emit("B")
        emit("C")
    }

    flow.collect { print(it) }
    flow.collect { print(it) }
}

// 마지막으로 타입에 상관없이 값을 방출하고 모으기 위해 String 을 제네릭 타입으로 바꾼다45

private suspend fun Flow<String>.testCounter() = flow {
    var counter = 0
    // println("Outside = " + Thread.currentThread().name) // main
    collect { // 콜렉션 단계에서 counter 종속적이면 오케이
        counter++
        List(100) { kotlin.random.Random(0) }.shuffled()
        // println("Inside = " + Thread.currentThread().name) // main
        emit(counter.toString())
    }
}

private fun Flow<String>.testCounterWrong(): Flow<String> {
    var counter = 0
    return this.map { // map 을 해버리면 플로우 자체에 종속된다
        counter++
        List(100) { kotlin.random.Random(0) }.shuffled()
        counter.toString()
    }
}

// 외부 변수는 플로우 콜렉션이 아니라 플로우 자체에 종속되면 동기화 문제 발생

private suspend fun testFlowCounter() {
    val flow : Flow<String> = List(1000) { "$it" }.asFlow().testCounter()

    // 플로우 생성됨 -> 생성된 애를 두번 콜렉트
    val flowWrong : Flow<String> = List(1000) { "$it" }.asFlow().testCounterWrong()

    coroutineScope {
        launch { println(flow.last()) }
        launch { println(flow.last()) }

        launch { println(flowWrong.last()) } // 1000
        launch { println(flowWrong.last()) } // 2000
    }
}