import java.sql.Timestamp
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*


fun main() {
    val df = SimpleDateFormat("yyyy MM dd")
    df.timeZone = TimeZone.getTimeZone("UTC")
    val d = df.format(Timestamp.from(Instant.now()))
    println(d)
    println(df.toLocalizedPattern())
    println(df.timeZone)

    val number = 12345.67
    // Format the number using the default locale
    val defaultFormat = NumberFormat.getInstance()
    val defaultFormattedNumber = defaultFormat.format(number)
    println("Default: $defaultFormattedNumber")

    // Format the number using the French locale
    val frenchLocale = Locale("fr", "FR")
    val frenchFormat = NumberFormat.getInstance(frenchLocale)
    val frenchFormattedNumber = frenchFormat.format(number)
    println("French: $frenchFormattedNumber")


    // Format the number using the German locale
    val germanLocale = Locale("de", "DE")
    val germanFormat = NumberFormat.getInstance(germanLocale)
    val germanFormattedNumber = germanFormat.format(number)
    println("German: $germanFormattedNumber")



    // Format the date using the default locale
    val currentDate = Date()
    val defaultFormat2 = DateFormat.getDateInstance()
    val defaultFormattedDate = defaultFormat2.format(currentDate)
    println("Default: $defaultFormattedDate")

    // Format the date using the French locale
    val frenchFormat2 = DateFormat.getDateInstance(DateFormat.DEFAULT, frenchLocale)
    val frenchFormattedDate = frenchFormat2.format(currentDate)
    println("French: $frenchFormattedDate")

    // Format the date using the German locale
    val germanFormat2 = DateFormat.getDateInstance(DateFormat.DEFAULT, germanLocale)
    val germanFormattedDate = germanFormat2.format(currentDate)
    println("German: $germanFormattedDate")

    val kf = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.KOREA)
    val kd = kf.format(currentDate)
    println("Korea : $kd")

    // locale 이 의미가 있는 SimpleDateFormat
    val sdf = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.KOREA)
    val formattedDate = sdf.format(currentDate)
    println(formattedDate)
}