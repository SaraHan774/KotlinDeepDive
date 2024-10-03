package coroutine_deepdive

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.produce

fun main() = runBlocking {
    /*val channel = produceNumbers()
    repeat(3) { id ->
        delay(10)
        launchProcessor(id, channel) // 채널은 원소를 기다리는 코루틴들을 FIFO 큐로 갖고 있다
    }*/

    /*val channel = Channel<String>() // one channel
    launch { sendString(channel, "foo", 200L) } // multiple producers
    launch { sendString(channel, "bar", 500L) }
    repeat(50) {
        println(channel.receive()) // consumer
    }
    // after receiving 50 elements, cancel the coroutines
    coroutineContext.cancelChildren()*/

    // pipeline
    val numbers = numbers()
    val squared = square(numbers)
    for (x in squared) {
        println(x)
    }
}

private suspend fun unlimitedChannel() {
    coroutineScope {
        val channel = produce(capacity = Channel.UNLIMITED) {
            // 용량이 무제한이면 채널은 모든 원소를 받고 수신자가 하나씩 가져가게 함
            repeat(5) { index ->
                send(index * 2)
                delay(100)
                println("Send")
            }
        }

        delay(1000)
        for (i in channel) {
            println("Receive $i")
            delay(1000)
        }
    }
}


private suspend fun bufferedChannel() {
    coroutineScope {
        val channel = produce(capacity = 3) {
            // 정해진 크기의 용량을 가지고 있다면 버퍼가 가득 찰 때까지 원소가 생성되고,
            //이후에 생성자는 수신자가 원소를 소비하기를 기다리기 시작함
            repeat(5) { index ->
                send(index * 2)
                delay(100)
                println("Send")
            }
        }

        delay(1000)
        for (i in channel) {
            println("Receive $i")
            delay(1000)
        }
    }
}

private suspend fun rendezvousChannel() = coroutineScope {
    val channel = produce(capacity = Channel.RENDEZVOUS) {
        println("producing value...")
        repeat(5) { index ->
            delay(100)
            val i = index * 2
            println("Send $i")
            send(i) // 송신자는 항상 수신자를 기다린다. 책 교환의 비유
        }
    }

    println("디레이 전")
    delay(1000)
    println("디레이 이후")

    repeat(5) {
        delay(3000)
        val i = channel.receive()
        println("$i 받음")
    }
}

private suspend fun conflatedChannel() = coroutineScope {
    val channel = produce(capacity = Channel.CONFLATED) {
        // 이전 원소를 더 이상 저장하지 않는다
        // 새로운 원소가 이전 원소를 대체, 최근 원소만 받을 수 있게 된다.
        // 따라서 먼저 보내진 원소가 유실된다.
        repeat(5) { index ->
            send(index * 2)
            delay(100)
            println("sent")
        }
    }

    delay(1000)

    for (i in channel) {
        println("Receive $i")
        delay(1000)
    }
}

// fan out
private fun CoroutineScope.produceNumbers() = produce {
    repeat(10) {
        delay(100)
        send(it)
    }
}

private fun CoroutineScope.launchProcessor(id: Int, channel: ReceiveChannel<Int>) = launch {
    for (msg in channel) {
        println("Processor #$id received $msg")
    }
}

// fan - in
private suspend fun sendString(channel: SendChannel<String>, text: String, time: Long) {
    while (true) {
        delay(time)
        channel.send(text)
    }
}

private fun <T> CoroutineScope.fanIn(channels: List<ReceiveChannel<T>>): ReceiveChannel<T> =
    produce { // 생성하는 하나의 채널 내부적으로 만들어
        for (channel in channels) {
            launch { // 여러개의 생성자를 만든다 ?
                for (msg in channel) {
                    send(msg) // 각 채널은 별개의 코루틴에서 send 를 수행하게 됨
                }
            }
        }
    }

// 1부터 3까지의 수를 가진 채널
private fun CoroutineScope.numbers(): ReceiveChannel<Int> = produce {
    repeat(3) {
        send(it + 1)
    }
}

private fun CoroutineScope.square(numbers: ReceiveChannel<Int>) = produce {
    for (num in numbers) {
        send(num * num)
    }
}
