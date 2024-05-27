interface PersonData {
    val name: String
    val age: Int
}

open class Person(override val name: String, override val age: Int): PersonData

data class Book(val title: String, val author: PersonData) {
    override fun toString(): String {
        return "$title by ${author.name}"
    }
}

class Alias(
    private val realIdentity: PersonData,
    private val newIdentity: PersonData,
): PersonData {
    override val name: String
        get() = newIdentity.name
    override val age: Int
        get() = newIdentity.age
}

class AliasDelegate(
    realIdentity: PersonData,
    private val newIdentity: PersonData
) : PersonData by newIdentity {
    override val age: Int
        get() = 300
}

fun PersonData.aliased(newIdentity: PersonData): PersonData {
    return object : PersonData by newIdentity {
        override val age: Int
            get() = this@aliased.age
    }
}

fun main() {
    val saraHan = Person("sara", 28)
    val alias =  Alias(saraHan, Person("alias sara", 30))
    val alias2 = AliasDelegate(saraHan, Person("delegate", 32))
    val book = Book("name of the book", alias2)

    saraHan.aliased(Person("gonny", 34))
//    println(book)
//    println(alias.name)
//    println(alias2.name)
}

