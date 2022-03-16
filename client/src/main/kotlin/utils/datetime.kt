package utils

import java.text.SimpleDateFormat
import java.util.*


// take in a datetime string as 2022-03-16T00:00:00.000 and return a string of the form
// 16 March 2022, 00:00 AM/PM
fun formatDateTime(datetime: String): String {
    print(datetime)
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ENGLISH)
    val date = sdf.parse(datetime)
    val sdf2 = SimpleDateFormat("d MMM yyyy 'at' h:mm a", Locale.ENGLISH)
    return sdf2.format(date)
}


