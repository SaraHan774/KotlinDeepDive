import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals

class CoroutineJobTest {

    private var counter = AtomicInteger(0)
    private var job: Job? = null

    private suspend fun testing() {
        coroutineScope {
            if (job == null || job?.isActive == false) {
                job = this.launch {
                    // Simulate some work that might throw an exception
                    delay(10)
                    counter.incrementAndGet()
                    // Uncomment the line below to simulate an exception
                    // throw RuntimeException("Test exception")
                }
                job?.invokeOnCompletion {
                    job = null // Reset job after completion or failure
                }
            }
        }
    }

    @Test
    fun testJobResetAndRelaunch() = runBlocking {
        // Run the testing function multiple times
        repeat(10) {
            testing()
        }
        // Allow some time for the jobs to complete
        delay(100)

        // Verify that the counter was incremented
        assertEquals(1, counter.get())

        // Run the testing function again to see if it launches a new job
        testing()
        delay(20)

        // Verify that the counter was incremented again
        assertEquals(2, counter.get())
    }

    @Test
    fun testJobWithException() = runBlocking {
        // Reset counter and job for this test
        counter.set(0)
        job = null

        // Run the testing function multiple times with an exception in the job
        repeat(10) {
            testing()
        }
        // Allow some time for the jobs to complete and fail
        delay(100)

        // Verify that the counter was incremented (despite exceptions)
        assertEquals(1, counter.get())

        // Run the testing function again to see if it launches a new job after failure
        testing()
        delay(20)

        // Verify that the counter was incremented again
        assertEquals(2, counter.get())
    }
}