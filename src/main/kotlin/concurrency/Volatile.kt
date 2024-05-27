package concurrency

class BackgroundTask {
    @Volatile
    var isDone = false
        private set

    private var counter = 0

    fun start() {
        println("started ${Thread.currentThread().name}")
        if (counter == 10000) return
        repeat(10000) {
            counter++;
        }
        isDone = true
    }
}

fun main() {
    val task = BackgroundTask()

    Thread {
        task.start()
        if (task.isDone) {
            println("DONE 1")
        }
    }.start()

    Thread {
        task.start()

        if (task.isDone) {
            println("DONE 2")
        }
    }.start()

    println()
    while (!task.isDone) {
        println("*")
    }
    println()
    println("==== FIN ====")
}