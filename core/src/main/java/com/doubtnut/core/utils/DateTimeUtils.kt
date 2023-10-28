package com.doubtnut.core.utils

import android.annotation.SuppressLint
import com.instacart.library.truetime.TrueTimeRx
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {
    @SuppressLint("SimpleDateFormat")
    fun stringToDate(dateString: String): Date {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        try {
            return format.parse(dateString)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return Date()
    }

    private val eventCalendar = Calendar.getInstance()
    private val sdfEvent = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US)
    private val sdfEventDate = SimpleDateFormat("dd-MM-yyyy", Locale.US)

    fun getEventTime(timeInMillis: Long = System.currentTimeMillis()): String {
        try {
            eventCalendar.timeInMillis = timeInMillis
            return sdfEvent.format(eventCalendar.time)
        } catch (e: Exception) {
        }
        return ""
    }

    fun getEventTimeDate(timeInMillis: Long = System.currentTimeMillis()): String {
        try {
            eventCalendar.timeInMillis = timeInMillis
            return sdfEventDate.format(eventCalendar.time)
        } catch (e: Exception) {
        }
        return ""
    }

    @Suppress("SpellCheckingInspection")
    fun getValidatedEventTime(timeInMillis: Long = System.currentTimeMillis()): String {
        try {
            eventCalendar.timeInMillis = timeInMillis
            val eventStr = sdfEvent.format(eventCalendar.time)

            if (eventStr.length != 19) return ""
            val split1 = eventStr.split(" ")
            if (split1.size != 2) return ""

            split1[0].let {
                if (it.length != 10) return ""

                val splitddMMyyyy = it.split("-")
                if (splitddMMyyyy.size != 3) return ""

                if (splitddMMyyyy[0].length != 2) return ""
                if (splitddMMyyyy[1].length != 2) return ""
                if (splitddMMyyyy[2].length != 4) return ""
            }

            split1[1].let {
                if (it.length != 8) return ""

                val splitHHmmss = it.split(":")
                if (splitHHmmss.size != 3) return ""

                if (splitHHmmss[0].length != 2) return ""
                if (splitHHmmss[1].length != 2) return ""
                if (splitHHmmss[2].length != 2) return ""
            }

            return eventStr
        } catch (e: Exception) {
        }
        return ""
    }

    fun formatMilliSecondsToTime(milliseconds: Long): String {

        val hoursText: String
        val minutesText: String
        val secondsText: String
        val daysText: String

        val seconds: Int = ((milliseconds / 1000) % 60).toInt()
        val minutes: Int = ((milliseconds / (1000 * 60)) % 60).toInt()
        val hours: Int = ((milliseconds / (1000 * 60 * 60)) % 24).toInt()
        val days: Int = (milliseconds / (1000 * 60 * 60 * 24)).toInt()
        hoursText = twoDigitString(hours)
        minutesText = twoDigitString(minutes)
        secondsText = twoDigitString(seconds)
        daysText = twoDigitString(days)

        return if (daysText == "00") {
            "${hoursText}h : ${minutesText}m : ${secondsText}s"
        } else {
            "${daysText}d : ${hoursText}h : ${minutesText}m : ${secondsText}s"
        }
    }

    private fun twoDigitString(number: Int): String {
        if (number == 0) {
            return "00"
        }
        if (number / 10 == 0) {
            return "0$number"
        }
        return number.toString()
    }

    fun getCurrentTimeInMillis(): Long {
        val date: Date =
            if (TrueTimeRx.isInitialized()) TrueTimeRx.now()
            else Calendar.getInstance().time
        return date.time
    }
}
