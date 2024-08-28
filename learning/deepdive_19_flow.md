### Flow 란 무엇인가

```kotlin
interface Flow<out T> {
    suspend fun collect(collector: FlowCollector<T>)
}

// 비슷한 애들 

interface Iterable<out T> {
    operator fun iterator(): Iterator<T>
}

interface Sequence<out T> {
    operator fun iterator(): Iterator<T> // suspend function 이 아님 -> 
}
```

- 여러개의 값을 반환하는 함수가 필요할 때
    - `list` `set` 과 같은 자료구조로 여러 값을 담아 반환하는 것 표현
    - `list` `set` 은 모든 원소들의 계산이 완료된 컬렉션
    - 원소들이 계산되고 채워지는 `생성시간`이 소요

```kotlin 
fun allUsers(): List<User> = api.getAllUsers().map { it.toUser() }

fun getList(): List<String> = List(3) {
    Thread.sleep(1000)
    "User $it"
}

fun main() {
    val list = getList()
    println("Function started")
    list.forEach { println(it) }
}
```

- 원소를 하나씩 계산할 때는 원소가 나오자마자 바로 얻을 수 있는 것이 낫다.
- Sequence 를 사용하는 편이 낫다.

```kotlin
fun getSequence(): Sequence<String> = sequence {
    repeat(3) {
        Thread.sleep(1000)
        yield("User $it")
    }
}

```

### Sequence 가 예기치 못하게 블로킹

```kotlin
fun getSequence(): Sequence<String> = sequence {
    repeat(3) {
        Thread.sleep(1000)
        yield("User$it")
    }
}

suspend fun main() {
    withContext(newSingleThreadContext("main")) {
        launch {
            repeat(3) {
                delay(100)
                println("Processing on coroutine")
            }
        }

        val list = getSequence()
        list.forEach { println(it) }
    }
}
```

#### 실행 결과 

```
User0
User1
User2
Processing on coroutine
Processing on coroutine
Processing on coroutine
```

### Flow 를 이용하면 결과가 반대 

```kotlin
fun getFlow(): Flow<String> = flow {
    repeat(3) {
        delay(1000)
        emit("User$it")
    }
}

suspend fun main() {
    withContext(newSingleThreadContext("main")) {
        launch {
            repeat(3) {
                delay(100)
                println("Processing on coroutine")
            }
        }
        val list = getFlow()
        list.collect { println(it) } // non - blocking 
    }
}
```

### Flow 를 사용하는 사례 

```kotlin
suspend fun getOffers(sellers: List<Seller>): List<Offer> = coroutineScope {
    sellers.map { seller -> 
        async { api.requestOffers(seller.id) }
    }.flatMap { it.await() }
}

// flow 를 사용하는 경우 

suspend fun getOffers(sellers: List<Seller>): List<Offer> =
    sellers.asFlow()
      .flatMapMerge(concurrency=20) { seller -> 
          suspend { api.requextOffers(seller.id) }.asFlow() 
      }.toList() 
```

