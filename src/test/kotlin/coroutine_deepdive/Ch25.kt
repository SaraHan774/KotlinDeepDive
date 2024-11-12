package coroutine_deepdive

import app.cash.turbine.testIn
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import javax.annotation.processing.Messager
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

// Testing flows

// 변환 함수

class ObserveAppointmentsService(
    private val appointmentRepository: AppointmentRepository
) {
    fun observeAppointments(): Flow<List<Appointment>> =
        appointmentRepository.observeAppointments()
            .filterIsInstance<AppointmentsEvent.AppointmentsUpdate>() // repository 의 함수를 데코레이트 한다
            .map { it.appointments }
            .distinctUntilChanged()
            .retry {
                it is ApiException && it.code in 500..599
            }

    // 기능에 따라 별개의 문장으로 설명 :
    // - 갱신된 약속만을 유지한다
    // - 이전 원소와 동일한 원소는 제거한다
    // - 5xx 에러 코드를 가진 API 예외가 발생한다면 재시도 한다

    // -> 테스트 하기 위해서는 AppointmentRepository 를 가짜로 만들거나, fake, mocking 해야한다

    // 시간이 중요하지 않으며, 한정된 플로우를 소스 플로우로 정의 flowOf ...
}

class ApiException(val code: Int, override val message: String? = null) : Throwable(message = message)


sealed class AppointmentsEvent {
    class AppointmentsUpdate(val appointments: List<Appointment>) : AppointmentsEvent()
    data object AppointmentsConfirmed : AppointmentsEvent()
}

interface AppointmentRepository {
    fun observeAppointments(): Flow<AppointmentsEvent>
}

data class Appointment(
    val name: String,
    val dateTime: Instant,
)

class FakeAppointmentsRepository(private val flow: Flow<AppointmentsEvent>) : AppointmentRepository {
    override fun observeAppointments() = flow
}

// 상태 플로우, 공유 플로우

class MessageService(messagesSource: Flow<Message>, scope: CoroutineScope) {
    private val source = messagesSource.shareIn(scope = scope, started = SharingStarted.WhileSubscribed())

    fun observeMessages(fromUserId: String) = source.filter { it.fromUserId == fromUserId }
}

data class Message(val fromUserId: String, val text: String)

class MessagesServiceTest {
    @Test
    fun `should emit messages from user`() = runTest {
        val source = flowOf(
            Message(fromUserId = "0", text = "A"),
            Message(fromUserId = "1", text = "B"),
            Message(fromUserId = "0", text = "C"),
        )

        val service = MessageService(
            messagesSource = source,
            scope = backgroundScope
        )

        // when
        // FIXME :  0 번 아이디 값으로 필터링한다 ? ---> 현실은 여기서 무한 기다리게 됨!!!
        val result = service.observeMessages("0").toList()

        // then
        assertEquals(
            listOf(
                Message(fromUserId = "0", text = "A"),
                Message(fromUserId = "0", text = "C"),
            ), result
        )
    }

    // backgroundScope 에서 플로우를 시작하고, 플로우가 방출하는 모든 값들을 컬렉션에 저장
    @Test
    fun `should emit messages from user corrected`() = runTest {
        // given
        val source = flow {
            emit(Message(fromUserId = "0", text = "A"))
            delay(1000)
            emit(Message(fromUserId = "1", text = "B"))
            emit(Message(fromUserId = "0", text = "C"))
        }

        val service = MessageService(
            messagesSource = source,
            scope = backgroundScope
        )

        // when
        val emittedMessages = mutableListOf<Message>()
        service.observeMessages("0")
            .onEach { emittedMessages.add(it) }
            .launchIn(backgroundScope)
        delay(1) // ---> ????

        // then
        assertEquals(
            listOf(
                Message(fromUserId = "0", text = "A"),
            ),
            emittedMessages
        )

        delay(1000) // 테스트 시간을 유연하게 ?

        assertEquals(
            listOf(
                Message(fromUserId = "0", text = "A"),
                Message(fromUserId = "0", text = "C"),
            ), emittedMessages
        )
    }

    // 짧은 시간 동안만 감지할 수 있는 toList ?
    // 유연성은 떨어지지만 가독성은 좋다...
    suspend fun <T> Flow<T>.toListDuring(duration: kotlin.time.Duration): List<T> = coroutineScope {
        val result = mutableListOf<T>()
        val job = launch {
            this@toListDuring.collect(result::add)
        }
        delay(duration)
        job.cancel()
        return@coroutineScope result
    }

    @Test
    fun `should emit messages from user short`() = runTest {
        val source = flow {
            emit(Message(fromUserId = "0", text = "A"))
            emit(Message(fromUserId = "1", text = "B"))
            emit(Message(fromUserId = "0", text = "C"))
        }

        val service = MessageService(
            messagesSource = source,
            scope = backgroundScope
        )

        val emittedMessages = service.observeMessages("0").toListDuring(1.milliseconds)

        assertEquals(listOf(
            Message("0", "A"),
            Message("0", "C"),
        ), emittedMessages)
    }

    @Test
    fun `should emit messages from user turbine`() = runTest {
        val source = flow {
            emit(Message(fromUserId = "0", text = "A"))
            emit(Message(fromUserId = "1", text = "B"))
            emit(Message(fromUserId = "0", text = "C"))
        }

        val service = MessageService(source, backgroundScope)

        // when
        val messagesTurbine = service.observeMessages("0").testIn(backgroundScope)
        // 메시지를 무언가에 담는다.
        // 지연이 있더라도 괜찮다.

        // then
        assertEquals(
            Message("0", "A"),
            messagesTurbine.awaitItem() // 아이템을 꺼낸다 ?
        )

        assertEquals(
            Message("0", "C"),
            messagesTurbine.awaitItem()
        )

        messagesTurbine.expectNoEvents() // --- ???
    }
}


class ObserveAppointmentsServiceTest {
    val aDate1 = Instant.parse("2020-01-01T00:00:00Z")
    val aDate2 = Instant.parse("2020-01-02T00:00:00Z")
    val anAppointment1 = Appointment("APP1", aDate1)
    val anAppointment2 = Appointment("APP2", aDate2)


    // 이런 테스트의 장점 : 테스트를 간단하게 만들 수 있다
    // 단점 : 플로우를 리스트처럼 다룬다.
    @Test
    fun `should keep only appointments from`() = runTest {
        // given
        val repo = FakeAppointmentsRepository(
            flowOf(
                AppointmentsEvent.AppointmentsConfirmed,
                AppointmentsEvent.AppointmentsUpdate(listOf(anAppointment1)),
                AppointmentsEvent.AppointmentsUpdate(listOf(anAppointment2)),
                AppointmentsEvent.AppointmentsConfirmed,
            )
        ) // fake 를 만들어서, 결과를 리스트로 변환한다.

        val service = ObserveAppointmentsService(repo)

        // when
        val result = service.observeAppointments().toList() // 리스트 변환된 결과로 어서트.

        // then
        assertEquals(
            listOf(
                listOf(anAppointment1),
                listOf(anAppointment2)
            ),
            result
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should eliminate elements that are`() = runTest {
        val repo = FakeAppointmentsRepository(
            flow {
                delay(1000) // 임의의 디레이 시간에 따른 값 방출을 검증한다
                emit(AppointmentsEvent.AppointmentsUpdate(listOf(anAppointment1)))
                emit(AppointmentsEvent.AppointmentsUpdate(listOf(anAppointment1)))
                delay(1000)
                emit(AppointmentsEvent.AppointmentsUpdate(listOf(anAppointment2)))
                delay(1000)
                emit(AppointmentsEvent.AppointmentsUpdate(listOf(anAppointment2)))
                emit(AppointmentsEvent.AppointmentsUpdate(listOf(anAppointment1)))
            }
        )

        val service = ObserveAppointmentsService(repo)

        // when
        val result = service.observeAppointments()
            .map { currentTime to it } // TestScope 에서 가져올 수 있는 현재 시간이다
            .toList()

        assertEquals(
            listOf(
                1000L to listOf(anAppointment1),
                2000L to listOf(anAppointment2),
                3000L to listOf(anAppointment1)
            ), result
        )
    }

    // 5xx 에러 코드를 가진 api 에러가 발생할 경우 재시도 해야 한다
    // -> 재시도 하는 플로우를 반환할 경우에는, 테스트하는 함수가 무한정 재시도하게 되며 끝나지 않는 플로우가 생성됨
    // 끝나지 않는 플로우를 테스트하는 가장 쉬운 방법은 take 를 사용해 원소의 수를 제한하는 것.
    @Test
    fun `should retry when API exception`() = runTest {
        // given
        val repo = FakeAppointmentsRepository(
            flow {
                emit(AppointmentsEvent.AppointmentsUpdate(listOf(anAppointment1)))
                throw ApiException(502, "Some message")
            }
        )
        val service = ObserveAppointmentsService(repo)

        // when
        val result = service.observeAppointments()
            .take(3) // 세번 리트라이, 재방출을 했을 것이므로 세개 아이템 나온다
            .toList()

        // then
        assertEquals(
            listOf(
                listOf(anAppointment1),
                listOf(anAppointment1),
                listOf(anAppointment1),
            ),
            result
        )
    }

    //  또 다른 방법은 플로우가 재시도해야 하는 예외를 먼저 던진 다음에,
    // 재시도 하지 않아야 하는 예외를 던지게 하는 것.
    @Test
    fun `should retry when API exception with code 5xx`() = runTest {
        var retried = false
        val someException = object : Exception() {}
        val repo = FakeAppointmentsRepository(
            flow {
                emit(AppointmentsEvent.AppointmentsUpdate(listOf(anAppointment1)))
                if (!retried) {
                    retried = true
                    throw ApiException(502, "Some message")
                } else {
                    throw someException
                }
            }
        )

        val service = ObserveAppointmentsService(repo)

        // when
        val result = service.observeAppointments()
            .catch<Any> { emit(it) }
            .toList() // toList 는 내부적으로 collect 한 후에 리스트로 변환

        // then
        assertTrue(retried)
        assertEquals(
            listOf(
                listOf(anAppointment1),
                listOf(anAppointment1), // first retry -> should re-emit
                someException, // second exception is not triggering retry.
            ), result
        )
    }

    // 끝나지 않는 플로우 테스트하기
    // 상태 플로우와 공유플로우를 사용하는 클래스를 테스트하는 건 훨씬 복잡하다.
    // 1. runTest 스코프는 this 가 아닌 backgroundScope. 테스트에서 스코프가 끝나길 기다릴 수 없음
    // 2. 상태 플로우와 공유 플로우는 무한정. 스코프가 취소되지 않는 한 플로우도 완료되지 않음

}