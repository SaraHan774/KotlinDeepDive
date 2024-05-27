fun interface IntPredicate {
    fun accept(i: Int) : Boolean // 인터페이스 한개짜리
}

fun interface HelloWorld {
    fun sayIt(name: String, date: String)
}

fun main() {
    val isEven = object : IntPredicate {
        override fun accept(i: Int): Boolean {
            return i % 2 == 0
        }
    }

    // Kotlin SAM Conversion 사용
    val isEven2 = IntPredicate { it % 2 == 0 }

    HelloWorld { name, date ->
        println()
    }
}