package com.doubtnutapp.utils

/**
 * Created by Anand Gaurav on 2019-07-31.
 */
object RedDotNewVisibilityUtil {

    @JvmStatic
    fun shouldShowRedDot(type: String?, state: Boolean?): Boolean {
        if (state != null && type != null && state && type.equals("red_dot")) {
            return true
        }
        return false
    }

    @JvmStatic
    fun shouldShowNewBadge(type: String?, state: Boolean?): Boolean {
        if (state != null && type != null && state && type.equals("new")) {
            return true
        }
        return false
    }

}