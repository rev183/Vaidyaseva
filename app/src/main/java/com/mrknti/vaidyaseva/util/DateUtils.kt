package com.mrknti.vaidyaseva.util

import com.mrknti.vaidyaseva.data.LOCALE_IN
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

object DateFormat {
    const val HOUR_DAY_MONTH = "h:mm a, dd MMM"
    const val DAY_MONTH = "dd/MM"
    const val DAY_MONTH_YEAR = "dd/MM/yy"
}

val TODAY_START: Long
    get() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        return calendar.timeInMillis
    }

fun convertToISO8601(date: Date): String {
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", LOCALE_IN)
    return dateFormatter.format(date)
}

fun Date.formatDate(pattern: String): String {
    val dateFormatter = SimpleDateFormat(pattern, LOCALE_IN)
    return dateFormatter.format(this)
}

fun Date.differenceInHours(): Long {
    val diff = this.time - Date().time
    return diff / (60 * 60 * 1000)
}