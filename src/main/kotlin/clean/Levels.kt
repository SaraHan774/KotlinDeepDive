package clean


// 저수준 컴포넌트 (입력 인터페이스)
interface CharReader {
    fun readChar(): Char
}

// 저수준 컴포넌트 (출력 인터페이스)
interface CharWriter {
    fun writeChar(c: Char)
}

// 콘솔 입력 클래스 (저수준)
class ConsoleReader : CharReader {
    override fun readChar(): Char {
        print("Enter a character: ")
        return readln().firstOrNull() ?: ' '  // 기본값 ' '
    }
}

// 콘솔 출력 클래스 (저수준)
class ConsoleWriter : CharWriter {
    override fun writeChar(c: Char) {
        println("Encrypted character: $c")
    }
}

// 고수준 컴포넌트 (암호화 정책)
interface Encryptor {
    fun encrypt(c: Char): Char
}

// Encryptor 구현 (고수준)
class CaesarEncryptor : Encryptor {
    override fun encrypt(c: Char): Char {
        return c + 1 // 간단한 시저 암호
    }
}

// 최상위 계층: 어플리케이션 서비스 (고수준)
class ApplicationService(
    private val reader: CharReader,
    private val writer: CharWriter,
    private val encryptor: Encryptor
) {
    fun execute() {
        val c = reader.readChar()
        val encryptedChar = encryptor.encrypt(c)
        writer.writeChar(encryptedChar)
    }
}

// 실행
fun main() {
    val reader = ConsoleReader()
    val writer = ConsoleWriter()
    val encryptor = CaesarEncryptor()

    val appService = ApplicationService(reader, writer, encryptor)
    appService.execute()
}