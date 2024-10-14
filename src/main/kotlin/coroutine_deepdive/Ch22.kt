package coroutine_deepdive

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.coroutineContext

fun main() = runBlocking {
    // testLaunchIn()
}

class MyError : Throwable("My Error")

suspend fun catchTest() {
    val flow = flow {
        emit(1)
        emit(2)
        throw MyError()
    }

    flow.onEach { println("onEach: $it") } // onEach 는 예외에 반응x, onCompletion 핸들러만 예외에 반응함.
        .catch { println("catch: $it") }
        .collect { println("collect: $it") }
    //onEach: 1
    //collect: 1
    //onEach: 2
    //collect: 2
    //catch: coroutine_deepdive.MyError: My Error
}

suspend fun catchTest2() {
    val flow = flow {
        emit("Message1")
        throw MyError()
    }

    flow.catch { println("catch: $it") }
        .collect { println("collect: $it") }
}

suspend fun catchTest3() {
    flowOf("Message2")
        .catch { emit("catch: $it") } // catch 함수 윗부분에서 던진 것에만 반응
        .onEach { throw MyError() }
        .collect { println("collect: $it") }
}

suspend fun present(place: String, message: String) {
    val ctx = coroutineContext
    val name = ctx[CoroutineName]?.name
    println("[$name] $message on $place")
}

fun messagesFlow() = flow {
    present("flow builder", "Message")
    emit("Message")
}

suspend fun testFlowOn() {
    val users = messagesFlow()
    //[Name3] Message on flow builder
    //[Name2] Message on onEach
    //[Name1] Message on collect
    withContext(CoroutineName("Name1")) {
        users
            .flowOn(CoroutineName("Name3"))
            .onEach { present("onEach", it) }
            .flowOn(CoroutineName("Name2"))
            .collect { present("collect", it) }
    }
}

suspend fun testLaunchIn() = coroutineScope {
    flowOf("User1", "User2", "User3")
        .onStart { println("Start") }
        .onEach { println(it) }
        .launchIn(this)
}