package com.doubtnutapp.utils

import android.annotation.SuppressLint
import android.text.format.DateFormat
import com.instacart.library.truetime.TrueTimeRx
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    @SuppressLint("SimpleDateFormat")
    fun formatStringDate(dateString: String): String {
        val formattedInputDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val date = formattedInputDate.parse(dateString)

        // Compare today's date with the input date, return "Today" if it matches otherwise formatted date (Ex 23 September - DD MMMM).
        val inputDay = DateFormat.format("dd", date)
        val inputMonth = DateFormat.format("MM", date)
        val inputYear = DateFormat.format("yyyy", date)

        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        if (String.format("%02d", currentDay) == inputDay && String.format("%02d", currentMonth) == inputMonth && currentYear.toString() == inputYear) {
            return "Today"
        } else {
            val formattedOutputDate = SimpleDateFormat("dd MMMM")
            return formattedOutputDate.format(date)
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun stringToDate(dateString: String): Date {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        try {
            return format.parse(dateString)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return Date()
    }

    fun formatDayTime(hourOfDay: Int, minute: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        val timeFormat = SimpleDateFormat("hh:mm a")
        return timeFormat.format(calendar.time)
    }

    fun formatDate(year: Int, month: Int, day: Int): String {
        val selectedDate = Calendar.getInstance().apply {
            set(year, month, day)
        }
        val df = SimpleDateFormat("dd/MM/yyyy")
        return df.format(selectedDate.time)
    }

    fun Date.isYesterday(): Boolean {
        return android.text.format.DateUtils.isToday(time + android.text.format.DateUtils.DAY_IN_MILLIS)
    }

    fun Date.isDayBeforeYesterday(): Boolean {
        return android.text.format.DateUtils.isToday(
            time + android.text.format.DateUtils.DAY_IN_MILLIS + android.text.format.DateUtils.DAY_IN_MILLIS
        )
    }

    fun Date.isTomorrow(): Boolean {
        return android.text.format.DateUtils.isToday(time - android.text.format.DateUtils.DAY_IN_MILLIS)
    }

    fun Date.isToday(): Boolean = android.text.format.DateUtils.isToday(time)


    fun getCurrentTime(): Long {
        val date: Date =
                if (TrueTimeRx.isInitialized()) TrueTimeRx.now()
                else Calendar.getInstance().time
        return date.time
    }

    fun isBeforeCurrentTime(timeInMillis: Long?): Boolean {
        if (timeInMillis == null) return false
        return timeInMillis < getCurrentTime()
    }

}


