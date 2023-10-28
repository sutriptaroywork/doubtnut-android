package com.doubtnutapp.data.common

import java.text.SimpleDateFormat
import java.util.*

open class DailyStreakDate(val date: String) {

    companion object {

        private val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy")

        fun getDailyStreakDate(date: Long): DailyStreakDate {
            return DailyStreakDate(simpleDateFormat.format(Date(date)))
        }
    }

    fun isValid() = date.isNotEmpty() || try {
        simpleDateFormat.format(date)
        true
    } catch (ex: Exception) {
        false
    }

    fun isNextDay(currentSavedDate: DailyStreakDate): Boolean {
        val thisDate = simpleDateFormat.parse(this.date)
        val currentDate = simpleDateFormat.parse(currentSavedDate.date)

        return thisDate > currentDate
    }
}
