package utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


// take in a datetime string as 2022-03-16T00:00:00.000 and return a string of the form
// 16 March 2022, 00:00 AM/PM
fun formatDateTime(datetime: String): String {
    val formatter = DateTimeFormatter.ofPattern("MMM dd yyyy, h:mm a")
    try {
        val date = LocalDateTime
            .parse(datetime)
        return date.format(formatter)
    } catch (e: Exception) {
        return datetime
    }
}


