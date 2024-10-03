package coroutine_deepdive

import kotlinx.coroutines.delay

private suspend fun myFunction(token: String) {
    println("Before")
    val userId = getUserId(token)
    println("UserId $userId")
    val userName = getUserName(userId, token)
    println(User(userId, userName))
    println("After")
}

data class User(val id: String, val name: String)

private suspend fun getUserId(token: String): String {
    delay(1000)
    return "some token"
}
private suspend fun getUserName(userId: String, token: String): String {
    delay(1000)
    return "some token $userId $token"
}