package com.doubtnutapp.utils

import androidx.core.content.edit
import com.doubtnutapp.Constants
import com.doubtnutapp.data.common.UserPreferenceImpl
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.orDefaultValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object UserUtil {

    fun getStudentId() = defaultPrefs().getString(Constants.STUDENT_ID, "").orDefaultValue()

    fun getStudentClass() = defaultPrefs().getString(Constants.STUDENT_CLASS, "").orDefaultValue()

    fun getStudentName() =
        defaultPrefs().getString(Constants.STUDENT_USER_NAME, "").orDefaultValue()

    fun getProfileImage() = defaultPrefs().getString(Constants.IMAGE_URL, "").orDefaultValue()

    fun getPhoneNumber() = defaultPrefs().getString(Constants.PHONE_NUMBER, "").orDefaultValue()

    fun setPhoneNumber(phoneNumber: String) =
        defaultPrefs().edit().putString(Constants.PHONE_NUMBER, phoneNumber).apply()

    fun getUserLanguage() =
        defaultPrefs().getString(Constants.STUDENT_LANGUAGE_NAME, "").orDefaultValue()

    fun getUserBoard() =
        defaultPrefs().getString(UserPreferenceImpl.USER_SELECTED_BOARD, "").orDefaultValue()

    fun getUserExams() =
        defaultPrefs().getString(UserPreferenceImpl.USER_SELECTED_EXAMS, "").orDefaultValue()

    fun getUserCcmId() = defaultPrefs().getString(UserPreferenceImpl.CCM_ID, "").orDefaultValue()

    fun getUserRestoreId() =
        defaultPrefs().getString(Constants.FRESHCHAT_RESTORE_ID, "").orDefaultValue()

    fun getConfigMap(): HashMap<String, Any>? {
        val hashString = defaultPrefs().getString(Constants.CLASS_CAMERA_DATA, "") ?: ""
        if (hashString.isBlank()) {
            return null
        }
        return Gson().fromJson(hashString, object : TypeToken<HashMap<String, Any>>() {}.type)
    }

    fun checkIsIITExamAnd11to13ClassUser(selectedExams: List<String>): Boolean {
        val userClass = getStudentClass()
        val validClass = userClass == "11" || userClass == "12" || userClass == "13"
        return validClass && selectedExams.contains("IIT JEE")
    }

    fun checkisClass9to12User(): Boolean {
        val userClass = getStudentClass()
        return userClass == "9" || userClass == "10" || userClass == "11" || userClass == "12"
    }

    fun checkisClass9to13User(): Boolean {
        val userClass = getStudentClass()
        return userClass == "9" || userClass == "10" || userClass == "11" || userClass == "12" || userClass == "13"
    }

    fun getTotalTimeSpentInMinute(): Long {
        return defaultPrefs().getLong(Constants.TIME_SPENT_IN_MIN, 0)
    }

    fun putTimeSpentInMinute(timeInMinute: Long) {
        return defaultPrefs().edit {
            putLong(Constants.TIME_SPENT_IN_MIN, timeInMinute)
        }
    }

    var countryCode: String
        get() = defaultPrefs().getString(Constants.COUNTRY_CODE, "").orDefaultValue()
        set(value) {
            defaultPrefs().edit().putString(Constants.COUNTRY_CODE, value).apply()
        }

    fun getIsAnonymousLogin(): Boolean {
        return defaultPrefs().getInt(Constants.ANONYMOUS_LOGIN_TYPE, 0) == 1
    }

    fun isAnonymousLoginAllowed(): Boolean {
        return defaultPrefs().getInt(Constants.ANONYMOUS_LOGIN_TYPE, 0) != 2
    }

    fun putAnonymousLoginNotAllowed() {
        defaultPrefs().edit().putInt(Constants.ANONYMOUS_LOGIN_TYPE, 2).apply()
    }


    fun putAnonymousLogin() {
        defaultPrefs().edit().putInt(Constants.ANONYMOUS_LOGIN_TYPE, 1).apply()
    }

    fun getAuthPageOpenCount(): Int {
        return defaultPrefs().getInt(Constants.AUTH_PAGE_OPEN_COUNT, 0)
    }

    fun putAuthPageOpenCount(count: Int) {
        defaultPrefs().edit().putInt(Constants.AUTH_PAGE_OPEN_COUNT, count).apply()
    }

    fun getVideoPageEventSentDay(): Int {
        return defaultPrefs().getInt(Constants.VIDEO_PAGE_EVENT_SENT_DAY, 0)
    }

    fun putVideoPageEventSentDay(count: Int) {
        defaultPrefs().edit().putInt(Constants.VIDEO_PAGE_EVENT_SENT_DAY, count).apply()
    }

    fun putGuestLogin(guestUser: Boolean) {
        defaultPrefs().edit().putBoolean(Constants.GUEST_LOGIN, guestUser).apply()
    }

    fun getIsGuestLogin() : Boolean {
        return defaultPrefs().getBoolean(Constants.GUEST_LOGIN, false)
    }
}