package clean

// Permissions에 대한 인터페이스
interface PermissionsProvider {
    fun hasPermission(userId: String, permission: String): Boolean
}

// Permissions 구현체
class Permissions : PermissionsProvider {
    private val userPermissions = mapOf(
        "user1" to listOf("read", "write"),
        "user2" to listOf("read")
    )

    override fun hasPermission(userId: String, permission: String): Boolean {
        return userPermissions[userId]?.contains(permission) ?: false
    }
}


// User 엔티티는 PermissionsProvider 인터페이스에 의존
class User(private val userId: String, private val permissionsProvider: PermissionsProvider) {
    fun canPerformAction(action: String): Boolean {
        return permissionsProvider.hasPermission(userId, action)
    }
}

fun main() {
    // Permissions 객체 생성 (구체적인 구현)
    val permissions = Permissions()

    // User 객체 생성 (인터페이스를 통해 의존성 주입)
    val user1 = User("user1", permissions)
    val user2 = User("user2", permissions)

    // 권한 확인
    println("User1 can read: ${user1.canPerformAction("read")}")  // 출력: true
    println("User1 can write: ${user1.canPerformAction("write")}") // 출력: true
    println("User2 can write: ${user2.canPerformAction("write")}") // 출력: false
}