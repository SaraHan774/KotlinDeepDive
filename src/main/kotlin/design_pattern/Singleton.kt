package design_pattern

class ChocolateBoiler private constructor(
    private var empty: Boolean,
    private var boiled: Boolean
){
    fun fill() {
        if (isEmpty()) {
            empty = false
            boiled = false
        }
    }

    fun drain() {
        if (isEmpty().not() &&  isBoiled()) {
            empty = true
        }
    }

    fun boil() {
        if (isEmpty().not() && isBoiled().not()) {
            boiled = true
        }
    }

    fun isEmpty() = empty
    fun isBoiled() = boiled

    companion object {
        fun newInstance(): ChocolateBoiler {
            return ChocolateBoiler(true, false)
        }
    }
}