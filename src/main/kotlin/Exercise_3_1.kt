fun main() {
    val composedFunction = compose(
        { it * 2 },
        { it * 4 }
    )

    val r = composedFunction(4)

    //3.3.2 Applying curried functions
    println(addTwoInts(3)(5))
    println(addTwoInts(3)(3)) // function returning function

    // 3.3.3
    println(squareOfTriple2(3))
}

fun double(x: Int): Int = x * 2

val double: (Int) -> Int = { x -> x * 2 }

val doubleThenIncrement: (Int) -> Int = { x ->
    val double = x * 2
    double + 1
}

// it 을 사용할 수 없다
val add: (Int, Int) -> Int = { x, y -> x + y }

val multiplyBy2: (Int) -> Int = { n -> double(n) }

val multiplyBy2UsingIt: (Int) -> Int = { double(it) }

// double function is called on the same object, class, or package as this function
val multiplyBy2UsingRef: (Int) -> Int = ::double

class MyClass {
    fun double(n: Int): Int = n * 2

    companion object {
        fun compDouble(n: Int) = n * 2
    }
}

val foo = MyClass()

val multiplyBy2FromClass: (Int) -> Int = foo::double

val multiplyBy2FromComp: (Int) -> Int = MyClass.Companion::compDouble
val multiplyBy2FromCompAlternative: (Int) -> Int = (MyClass)::compDouble
// val multiplyBy2FromCompAlternative2: (MyClass, Int) -> Int = MyClass::compDouble


// ### Composing functions

fun square(n: Int) = n * n
fun triple(n: Int) = n * 3

fun compose(n: Int) = square(triple(n))

fun compose(f: (Int) -> Int, g: (Int) -> Int): (Int) -> Int = { x -> f(g(x)) }
fun composeSimple(f: (Int) -> Int, g: (Int) -> Int): (Int) -> Int = { f(g(it)) }
val squareOfTriple = compose(::square, ::triple)

// 3.2
// Make the compose function polymorphic by using type parameters.

fun <T> composePoly(f: (T) -> T, g: (T) -> T): (T) -> T = { f(g(it)) }

// 같은 타입으로만 받는게 아니라, T -> U -> V 로 이어지는 합성 함수의 꼴이어야 한다
// 그래서 리턴 타입은 T -> V 인 람다가 된다
// Int 이외의 다른 타입도 받을 수 있고, f, g 의 순서가 바뀌면 컴파일이 되지 않음 !
fun <T, U, V> composePolySolution(f: (U) -> V, g: (T) -> U): (T) -> V = { f(g(it)) }

// ### Advanced function features
// why do you need  functions represented as data ?
// multi-argument functions

// (Int) -> (Int) -> Int

// (Int) argument /  (Int) -> Int return type

// 3.3 Write a function to add two Int values.

/*
fun addTwoInts(n: Int): (Int) -> Int {
    return { n }
}
*/

typealias IntBinOp = (Int) -> (Int) -> Int

val addTwoInts: IntBinOp = { a -> { b -> a + b } }
val mult: IntBinOp = { a -> { b -> a + b } }


// Exercise 3.4
// Write a value function to compose two functions;
// for example, the square and triple functions used previously.

// f ( g ( x) )
val composeTwoFunctions: ((Int) -> Int) -> ((Int) -> Int) -> ((Int) -> Int) =
    { x -> { y -> { z -> x(y(z)) } } }

val composeTwoFunctionsTypeInf =
    { x: (Int) -> Int -> { y: (Int) -> Int -> { z: Int -> x(y(z)) } } }

typealias IntUnaryOp = (Int) -> Int

val composeIntUnaryOp: (IntUnaryOp) -> (IntUnaryOp) -> IntUnaryOp =
    { x -> { y -> { z -> x(y(z)) } } }

val square2: IntUnaryOp = { it * it }
val triple: IntUnaryOp = { it * 3 }
val squareOfTriple2 = composeIntUnaryOp(square2)(triple)
// pay attention to the order of the parameters:
// triple is applied first and then square is applied to the result by triple.

// 3.5
// Write a polymorphic version of the compose function.

//But this isn’t possible because Kotlin doesn’t allow standalone parameterized properties.
/*val <T, U, V> higherCompose: ((U) -> V) -> ((T) -> U) -> (T) -> V
= { f ->
    { g ->
        { x -> f(g(x))}
    }
}*/

fun <T, U, V> higherCompose(): ((U) -> V) -> ((T) -> U) -> (T) -> V = { f ->
    { g ->
        { x -> f(g(x)) }
    }
}

fun <T, U, V> higherCompose2() = { f: (U) -> V ->
    { g: (T) -> U ->
        { x: T ->
            f(g(x))
        }
    }
}

val squareOfTriple3 = higherCompose2<Int, Int, Int>()(square2)(triple)

// Exercise 3.6 (easy now!)
//Write the higherAndThen function that composes the functions the other way around,
// which means that higherCompose(f, g) is equivalent to higherAndThen(g, f).

// 위에 higherCompose 함수랑 비교해보면 인자로 받는 함수의 순서가 바뀌어있다
// 가장 내부에 있는 함수부터 먼저 평가되면서 바깥으로 연산을 진행한다 (기억)
fun <T, U, V> higherAndThem(): ((T) -> U) -> ((U) -> V) -> (T) -> V = { f ->
    { g ->
        { x: T ->
            g(f(x)) // V
        }
    }
}


// 3.3.5 Using anonymous functions
// 보통은 익명으로 많이 사용한다
val f: (Double) -> Double = { Math.PI / 2 - it }
val sin: (Double) -> Double = Math::sin
val cos: Double = composePoly(f, sin)(2.0)

// f 에 대해서 익명 함수로 넣어 볼 수 있다
val cosValue: Double = composePoly({ x: Double -> Math.PI - x }, Math::sin)(2.0)

//higherCompose is defined in curried form, applying one parameter at a time.
val cosHOF = higherCompose<Double, Double, Double>()({ x: Double -> Math.PI / 2 - x })(Math::sin)
val cosValueHOF = cosHOF(2.0)

// Don’t worry about the creation of anonymous functions.
// Kotlin won’t always create new objects each time the function is called
fun cos(arg: Double) = composePoly({ x -> Math.PI / 2 - x }, Math::sin)(arg)

// Implementing type inference
// type inference can also be an issue with anonymous functions
// the types of the two anonymous functions can be inferred by the compiler
// because it knows that the compose function takes two functions as arguments
fun <T, U, V> compose2(f: (U) -> V, g: (T) -> U): (T) -> V = { f(g(it)) }

fun cos2(arg: Double) = compose2({ x: Double -> Math.PI / 2 - x }, { y: Double -> Math.sin(y) })(arg)

val taxRate = 0.09
fun addTax(price: Double) = price + price * taxRate

// make it more modular
//functions of tuples of arguments

fun addTax(taxRate: Double, price: Double) = price + price * taxRate

// This applies to value functions
val addTax = { taxRate: Double, price: Double -> price + price * taxRate }

// addTax function takes a single argument, which is a pair of Double
// unlike Java, Kotlin allows the use of arguments of cardinality greater than 2.
// In Java, you can use the Function interface for a single argument
// and the BiFunction interface for a pair of arguments
// if you want triples or more, you have to define your own interfaces

// curried version
// curried function takes a single argument and returns a function taking a single argument,
// returning ... and so on until returning the final value.
val addTax2 = { taxRate: Double ->
    { price: Double ->
        price + price * taxRate
    }
}

// println(addTax(taxRate)(12.0))

class TaxComputer(private val rate: Double) {
    fun compute(price: Double): Double = price * rate + price
}

/*Exercise 3.7 (easy)
Write a fun function to partially apply a curried function of two arguments to its first argument.*/
fun <A, B, C> partialA(a: A, f: (A) -> (B) -> C): (B) -> C = f(a)

/*Exercise 3.8
Write a fun function to partially apply a curried function of two arguments to its second argument.*/
fun <A, B, C> partialB(b: B, f: (A) -> (B) -> C): (A) -> C = { a ->
    f(a)(b)
}

/*Exercise 3.9 (easy)
Convert the following function into a curried function:*/
fun <A, B, C, D> func(a: A, b: B, c: C, d: D): String = "$a, $b, $c, $d"

// fun <A, B, C, D> funcCurry(a: A, f: (A) -> (B) -> (C) -> D): String = {}

fun <A, B, C, D> curried(): (A) -> (B) -> (C) -> (D) -> String = { a ->
    { b ->
        { c ->
            { d ->
                "$a $b $c $d"
            }
        }
    }
}













