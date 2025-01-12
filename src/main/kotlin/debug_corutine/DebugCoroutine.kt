package debug_corutine

import kotlinx.coroutines.*

class DebugCoroutine {
    fun run() {
        val pool = newFixedThreadPoolContext(3, "myPool")
        runBlocking(pool + CoroutineName("main")) {
            repeat(6) {
                launch { describe("Kotlin") }
            }
        }
    }

    suspend fun describe(identifier: String) : String {
        val description = fetchDescription(identifier)
        val enhacedDescription = enhanceDescription(description)
        return enhacedDescription
    }

    private suspend fun fetchDescription(identifier: String): String {
        delay(1000)
        return "Description ID #$identifier"
    }

    private suspend fun enhanceDescription(description: String): String {
        delay(1000)
        return "Enhanced description $description"
    }
}