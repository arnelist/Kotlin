import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

fun formatDateWithWeekday(date: String): String {
    return try {
        val d = LocalDate.parse(date) // yyyy-MM-dd
        val day = d.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("lt", "LT"))
        "$date ($day)"
    } catch (_: Exception) {
        date
    }
}