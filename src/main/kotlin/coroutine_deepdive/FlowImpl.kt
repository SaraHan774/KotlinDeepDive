package coroutine_deepdive

import kotlinx.coroutines.delay

val f: () -> Unit = {
    print("A")
    print("B")
    print("C")
}

val f2: suspend () -> Unit = {
    print("A")
    delay(100)
    print("B")
    delay(100)
    print("C")
}

val f3: suspend ((String) -> Unit) -> Unit = { emit ->
    emit("A")
    emit("B")
    emit("C")
}

fun interface FlowCollector { // 함수형 인터페이스 정의
    suspend fun emit(value: String)
}

val f4: suspend (FlowCollector) -> Unit = {
    it.emit("A")
    it.emit("B")
    it.emit("C")
}

// it 해주는 것도 귀찮으니까 FlowCollector 를 리시버로 만들어 보자

val f5: suspend FlowCollector.() -> Unit = {
    emit("A")
    emit("B")
    emit("C")
}

// 람다식을 전달하는 대신에, 인터페이스를 구현한 객체를 만들자

interface Flow {
    suspend fun collect(collector: FlowCollector)
}

val builder: suspend FlowCollector.() -> Unit = {
    emit("A")
    emit("B")
    emit("C")
}

val flow = object : Flow {
    override suspend fun collect(collector: FlowCollector) {
        collector.builder()
    }
}

private fun flow(builder: suspend FlowCollector.() -> Unit) = object: Flow {
    override suspend fun collect(collector: FlowCollector) {
        collector.builder()
    }
}

suspend fun main() {
     flow {
         emit("A")
         emit("B")
     }
}