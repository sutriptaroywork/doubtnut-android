package com.doubtnutapp.data.gamification.userProfile

object PointsFormat {

    fun Format(number: Int?): String {
        val suffix = arrayOf("K", "M", "B", "T")
        var size = if (number!!.toInt() != 0) Math.log10(number.toDouble()).toInt() else 0
        if (size >= 3) {
            while (size % 3 != 0) {
                size = size - 1
            }
        }
        val notation = Math.pow(10.0, size.toDouble())
        return if (size >= 3) (+(Math.round(number / notation * 100) / 100.0)).toString() + suffix[size / 3 - 1] else (+number).toString() + ""
    }
}
