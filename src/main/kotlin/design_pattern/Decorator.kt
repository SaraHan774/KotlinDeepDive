package design_pattern

enum class Size { TALL, GRADE, VENTI }

abstract class Beverage {
    private var size: Size = Size.TALL

    fun setSize(size: Size) { this.size = size }

    fun getSize(): Size { return this.size }

    open fun getDescription(): String {
        return "Empty"
    }

    abstract fun cost(): Double
}

abstract class CondimentDecorator(val beverage: Beverage) : Beverage()

class Milk(beverage: Beverage) : CondimentDecorator(beverage) {
    override fun cost(): Double {
        // size 가 추가되는 경우
        val sizeExtraCharge = when(beverage.getSize()) {
            Size.TALL -> 0.5
            Size.GRADE -> 0.75
            Size.VENTI -> 2.0
        }
        return 10.0 + beverage.cost() + sizeExtraCharge
    }

    override fun getDescription(): String {
        return "${beverage.getDescription()}, 우유"
    }
}

class Mocha(beverage: Beverage) : CondimentDecorator(beverage) {
    override fun cost(): Double {
        return 2.0 + beverage.cost()
    }

    override fun getDescription(): String {
        return "${beverage.getDescription()}, 모카"
    }
}

class Whip(beverage: Beverage) : CondimentDecorator(beverage) {
    override fun cost(): Double {
        return 5.0 + beverage.cost()
    }

    override fun getDescription(): String {
        return "${beverage.getDescription()}, 휘핑크림"
    }
}

class HouseBlend : Beverage() {
    override fun getDescription(): String {
        return "하우스브렌드"
    }

    override fun cost(): Double {
        return 20.5
    }
}

class Espresso : Beverage() {
    override fun getDescription(): String {
        return "에스프레소"
    }

    override fun cost(): Double {
        return 10.4
    }
}

fun main() {
    val houseBlend = HouseBlend()
    houseBlend.setSize(Size.VENTI)

    val milk = Milk(houseBlend)
    val whip = Whip(milk)
    val mocha = Mocha(whip)

    println(mocha.cost())
    println(mocha.getDescription())
}