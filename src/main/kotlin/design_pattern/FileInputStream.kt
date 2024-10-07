package design_pattern

import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.FilterInputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

fun main() {
    // java.io 패키지의 InputStream 들도 데코레이퍼 패턴으로 구성되어 있음
    val fileInput = FileInputStream("/somefile.txt")
    val bufferedInput = BufferedInputStream(fileInput)
    val zipInput = ZipInputStream(bufferedInput)
    zipInput.read()
}

class LowerCaseInputStream(private val inputStream: InputStream) : FilterInputStream(inputStream) {
    override fun read(): Int {
        val c = inputStream.read()
        return if (c == -1) c else Character.toLowerCase(c)
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val result = inputStream.read(b, off, len)
        for (i in off until off + result) {
            b[i] = Character.toLowerCase(b[i].toChar()).toByte()
        }
        return result
    }
}