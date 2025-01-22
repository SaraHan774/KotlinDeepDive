package clean

// 순환 의존성을 제거할 수 있는 방법
// 1. 의존성 역전 원칙

interface AuthorizerInterface {
    fun authorize(userId: String): Boolean
}

class Authorizer : AuthorizerInterface {
    override fun authorize(userId: String): Boolean {
        // 인증 로직 구현 (예: 데이터베이스 확인, 토큰 검증 등)
        println("Authorizing user with ID: $userId")
        return userId == "validUser"
    }
}

class UserService(private val authorizer: AuthorizerInterface) { // Authorizer 자체의 변경으로부터 자유로워 진다
    fun performAction(userId: String) {
        if (authorizer.authorize(userId)) {
            println("Action performed for user: $userId")
        } else {
            println("Authorization failed for user: $userId")
        }
    }
}

fun main() {
    // Authorizer 객체 생성
    val authorizer = Authorizer()

    // UserService에 AuthorizerInterface 구현체 주입
    val userService = UserService(authorizer)

    // 메서드 호출
    userService.performAction("validUser")  // 성공
    userService.performAction("invalidUser") // 실패
}