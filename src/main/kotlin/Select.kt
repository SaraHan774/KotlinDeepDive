import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.selects.select
import java.time.Duration

fun main() {
    runBlocking {
        // println(askMultipleForData2())
        // channelsTest1()
        channelsTest()
    }
}

private suspend fun CoroutineScope.channelsTest1() {
    val fooChannel = produceString("foo", 210L)
    val barChannel = produceString("bar", 300L)

    repeat(7) {
        select {
            fooChannel.onReceive {
                println("from foo channel : $it")
            }
            barChannel.onReceive {
                println("from bar channel : $it")
            }
        }
    }

    coroutineContext.cancelChildren()
}


suspend fun requestData1(): String {
    delay(1000)
    return "data1"
}

suspend fun requestData2(): String {
    delay(2000)
    return "data2"
}

val scope = CoroutineScope(SupervisorJob())
suspend fun askMultipleForData(): String {
    val defData1 = scope.async { requestData1() }
    val defData2 = scope.async { requestData2() }
    return select {
        defData1.onAwait { it }
        defData2.onAwait { it }
    }
}

suspend fun askMultipleForData2(): String = coroutineScope {
    select {
        async { requestData1() }.onAwait { it }
        async { requestData2() }.onAwait { it }
    }.also { coroutineContext.cancelChildren() }
}

suspend fun CoroutineScope.produceString(s: String, time: Long) = produce {
    while (true) {
        delay(time)
        send(s)
    }
}

suspend fun CoroutineScope.channelsTest() {
    val c1 = Channel<Char>(capacity = 2)
    val c2 = Channel<Char>(capacity = 2)
    launch {
        for (c in 'A'..'H') {
            delay(500)
            select<Unit> {
                // onSend : selected when this channel has space in the buffer
                c1.onSend(c) {
                    println("Sent $c to 1")
                }
                c2.onSend(c) {
                    println("Sent $c to 2")
                }
            }
        }
    }

    launch {
        while (true) {
            delay(1000)
            val c = select<String> {
                // onReceive: selected when this channel has a value
                c1.onReceive {
                    "$it from 1"
                }
                c2.onReceive {
                    "$it from 2"
                }
            }
            println("Received $c")
        }
    }
}

val eventProducerChannel = Channel<String>()
fun CoroutineScope.receiverJob() = launch {
    val tickerChannel = ticker(1000)
    val events = mutableListOf<String>()
    try {
        while (true) {
            var hasTimeout = false
            val tickerJob = launch {
                tickerChannel.receive()
                hasTimeout = true
            }

            while (events.size < 500 && !hasTimeout) {
                withTimeoutOrNull(10) { eventProducerChannel.receive() }?.let {
                    events.add(it)
                }
            }

            events.forEach {
                println("received $it, ts : ${System.currentTimeMillis()}")
            }
            events.clear()
            tickerJob.cancel()
            hasTimeout = false
        }
    } finally {
        tickerChannel.cancel()
    }
}

fun <T> Flow<T>.bufferTimeout(size: Int, duration: Duration): Flow<List<T>> {
    require(size > 0) {
        "Window size should be greater than 0"
    }
    require(duration.toMillis() > 0) {
        "Duration should be greater than 0"
    }

    return flow {
        coroutineScope {
            val events = ArrayList<T>(size)
            val tickerChannel = ticker(duration.toMillis())
            try {
                var hasTimeout = false
                // collect from the upstream flow
                // produce will create channel
                val upstreamValues: ReceiveChannel<T> = produce { collect { send(it) } }

                while (isActive) {
                    val tickerJob = launch {
                        tickerChannel.receive()
                        hasTimeout = true
                    }

                    withTimeoutOrNull(10) {
                        upstreamValues.receive()?.let {
                            events.add(it)
                        }
                    }

                    if (events.size == size || hasTimeout && events.isNotEmpty()) {
                        emit(events.toList())
                        events.clear()
                        tickerJob.cancel()
                        hasTimeout = false
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                emit(events.toList())
            } finally {
                tickerChannel.cancel()
            }
        }
    }
}

// https://dev.to/psfeng/a-story-of-building-a-custom-flow-operator-buffertimeout-4d95
fun <T> Flow<T>.bufferTimeout2(size: Int, duration: Duration): Flow<List<T>> {
    require(size > 0) {
        "Window size should be greater than 0"
    }
    require(duration.toMillis() > 0) {
        "Duration should be greater than 0"
    }

    return flow {
        coroutineScope {
            val events = ArrayList<T>(size)
            val tickerChannel = ticker(duration.toMillis())
            try {
                val upstreamValues: ReceiveChannel<T> = produce { collect { send(it) } }

                while (isActive) {
                    var hasTimeout = false
                    select<Unit> {
                        upstreamValues.onReceive {
                            events.add(it)
                        }
                        tickerChannel.onReceive {
                            hasTimeout = true
                        }
                    }

                    if (events.size == size || hasTimeout && events.isNotEmpty()) {
                        emit(events.toList())
                        events.clear()
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                emit(events.toList())
            } finally {
                tickerChannel.cancel()
            }
        }
    }
}