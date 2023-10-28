package com.doubtnut.core.utils

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class DateTimeUtilsTest {

    private lateinit var todayCalendar: Calendar
    private lateinit var calendar: Calendar
    private lateinit var sdfEvent: SimpleDateFormat

    @Before
    fun getCalendar() {
        todayCalendar = Calendar.getInstance()
        calendar = Calendar.getInstance()
        sdfEvent = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US)
    }

    @After
    fun clearCalendar() {
        todayCalendar.clear()
        calendar.clear()
    }

    @Test
    fun stringToDate_validString_returnsDate() {
        val date = DateTimeUtils.stringToDate("1993-12-16T23:55:00.000Z")

        calendar.time = date

        assertThat(date.time).isEqualTo(calendar.timeInMillis)

        assertThat(calendar[Calendar.YEAR]).isEqualTo(1993)
        assertThat(calendar[Calendar.MONTH]).isEqualTo(Calendar.DECEMBER)
        assertThat(calendar[Calendar.DATE]).isEqualTo(16)
        assertThat(calendar[Calendar.DAY_OF_WEEK]).isEqualTo(Calendar.THURSDAY)
        assertThat(calendar[Calendar.HOUR_OF_DAY]).isEqualTo(23)
        assertThat(calendar[Calendar.HOUR]).isEqualTo(11)
        assertThat(calendar[Calendar.AM_PM]).isEqualTo(Calendar.PM)
        assertThat(calendar[Calendar.MINUTE]).isEqualTo(55)
        assertThat(calendar[Calendar.SECOND]).isEqualTo(0)
        assertThat(calendar[Calendar.MILLISECOND]).isEqualTo(0)
    }

    @Test
    fun stringToDate_invalidString_returnsCurrentDate() {
        val date = DateTimeUtils.stringToDate("1993 12-16T23:55:00.000Z")

        calendar.time = date

        assertThat(calendar[Calendar.YEAR]).isEqualTo(todayCalendar[Calendar.YEAR])
        assertThat(calendar[Calendar.MONTH]).isEqualTo(todayCalendar[Calendar.MONTH])
        assertThat(calendar[Calendar.DATE]).isEqualTo(todayCalendar[Calendar.DATE])
        assertThat(calendar[Calendar.DAY_OF_WEEK]).isEqualTo(todayCalendar[Calendar.DAY_OF_WEEK])
        assertThat(calendar[Calendar.HOUR_OF_DAY]).isEqualTo(todayCalendar[Calendar.HOUR_OF_DAY])
        assertThat(calendar[Calendar.HOUR]).isEqualTo(todayCalendar[Calendar.HOUR])
        assertThat(calendar[Calendar.MINUTE]).isEqualTo(todayCalendar[Calendar.MINUTE])
        assertThat(calendar[Calendar.AM_PM]).isEqualTo(todayCalendar[Calendar.AM_PM])
    }

    @Test
    fun stringToDate_emptyString_returnsCurrentDate() {
        val date = DateTimeUtils.stringToDate("")

        calendar.time = date

        assertThat(calendar[Calendar.YEAR]).isEqualTo(todayCalendar[Calendar.YEAR])
        assertThat(calendar[Calendar.MONTH]).isEqualTo(todayCalendar[Calendar.MONTH])
        assertThat(calendar[Calendar.DATE]).isEqualTo(todayCalendar[Calendar.DATE])
        assertThat(calendar[Calendar.DAY_OF_WEEK]).isEqualTo(todayCalendar[Calendar.DAY_OF_WEEK])
        assertThat(calendar[Calendar.HOUR_OF_DAY]).isEqualTo(todayCalendar[Calendar.HOUR_OF_DAY])
        assertThat(calendar[Calendar.HOUR]).isEqualTo(todayCalendar[Calendar.HOUR])
        assertThat(calendar[Calendar.MINUTE]).isEqualTo(todayCalendar[Calendar.MINUTE])
        assertThat(calendar[Calendar.AM_PM]).isEqualTo(todayCalendar[Calendar.AM_PM])
    }

    @Test
    fun getEventTime_returnsFormattedCurrentDateTimeString() {
        assertThat(DateTimeUtils.getEventTime()).isEqualTo(sdfEvent.format(todayCalendar.time))
    }

    @Test
    fun getEventTime_timeInLong_returnsFormattedDateTimeString() {
        val timeInMillis = 937206840000
        assertThat(DateTimeUtils.getEventTime(timeInMillis)).isEqualTo("13-09-1999 12:44:00")
    }

    @Test
    fun getValidatedEventTime_returnsValidatedCurrentDateTimeString() {
        assertThat(DateTimeUtils.getValidatedEventTime()).isEqualTo(sdfEvent.format(todayCalendar.time))
    }

    @Test
    fun getValidatedEventTime_timeInLong_returnsValidatedCurrentDateTimeString() {
        val timeInMillis = 253370763000000
        assertThat(DateTimeUtils.getValidatedEventTime(timeInMillis)).isEqualTo("01-01-9999 05:00:00")
    }

    @Test
    fun getValidatedEventTime_invalidTimeInLongWithWrongYear_returnsEmptyString() {
        // year = 9999 > 4 digits
        val timeInMillis = 3093496443000000
        assertThat(DateTimeUtils.getValidatedEventTime(timeInMillis)).isEqualTo("")
    }
}
