package com.doubtnut.core.utils

import com.doubtnut.core.constant.CoreConstants

object CoreUserUtils {

    fun getStudentId() =
        defaultPreferences().getString(CoreConstants.STUDENT_ID, "") ?: ""

    fun getStudentClass() = defaultPreferences().getString(CoreConstants.STUDENT_CLASS, "") ?: ""

    fun getUserBoard() =
        defaultPreferences().getString(CoreConstants.USER_SELECTED_BOARD, "") ?: ""
}