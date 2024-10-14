package coroutine_deepdive

fun main() {
    test()
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
    val intRange = 1 .. 3
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

    val result2 = intRange.flatMap {  a ->
        intRange.map { b -> a to b }
    }
    println("flatMap")
    println(result2)
    //[(1, 1), (1, 2), (1, 3), (2, 1), (2, 2), (2, 3), (3, 1), (3, 2), (3, 3)]
}