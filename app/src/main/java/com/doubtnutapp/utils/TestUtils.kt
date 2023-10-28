package com.doubtnutapp.utils

import com.doubtnutapp.Constants
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by akshaynandwana on
 * 29, January, 2019
 **/
object TestUtils {

    fun getTrueTimeDecision(publishTime: String?, unpublishTime: String?, now: Date): String {

        val readFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val trueTime = readFormat.parse(readFormat.format(now.time))
        val startTestTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(publishTime)
        val endTestTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(unpublishTime)

        val flag = if (trueTime.after(endTestTime)) {
            Constants.TEST_OVER
        } else if (trueTime.before(startTestTime)) {
            Constants.TEST_UPCOMING
        } else {
            Constants.TEST_ACTIVE
        }
        return flag
    }

}