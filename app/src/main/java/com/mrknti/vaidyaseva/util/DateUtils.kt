package com.mrknti.vaidyaseva.util

import com.mrknti.vaidyaseva.data.LOCALE_IN
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

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