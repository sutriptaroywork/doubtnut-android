package com.doubtnutapp.data.common

import org.junit.Test

class DailyStreakDateTest {

    @Test
    fun isValid() {
    }

    @Test
    fun isNextDay() {
        val currentDate = DailyStreakDate("01-01-1990")
        val nextDate = DailyStreakDate("02-01-1990")

        assert(nextDate.isNextDay(currentDate))
    }

    @Test
    fun isNotNextDay() {
        val currentDate = DailyStreakDate("02-01-1990")
        val nextDate = DailyStreakDate("02-01-1990")

        assert(!nextDate.isNextDay(currentDate))
    }
}
