package com.doubtnutapp.utils

import com.apxor.androidsdk.core.ApxorSDK
import com.apxor.androidsdk.core.Attributes
import com.doubtnut.core.utils.ThreadUtils
import com.doubtnutapp.*
import com.doubtnutapp.data.common.UserPreferenceImpl

object ApxorUtils {

    val userInfo: Attributes = Attributes()

    fun addDefaultAttributes() {
        ThreadUtils.runOnAnalyticsThread {
            userInfo.putAttribute("class", defaultPrefs().getString(Constants.STUDENT_CLASS, ""))
            userInfo.putAttribute("studentId", defaultPrefs().getString(Constants.STUDENT_ID, ""))
            userInfo.putAttribute(
                "language",
                defaultPrefs().getString(Constants.STUDENT_LANGUAGE_NAME, "")
            )
            userInfo.putAttribute(
                "exams",
                defaultPrefs().getString(UserPreferenceImpl.USER_SELECTED_EXAMS, "") ?: ""
            )
            userInfo.putAttribute(
                "board",
                defaultPrefs().getString(UserPreferenceImpl.USER_SELECTED_BOARD, "") ?: ""
            )
        }
    }

    fun addAppExperimentAttributes() {
        ThreadUtils.runOnAnalyticsThread {
            Features.capabilities.keys.forEach {
                val featureVariant = FeaturesManager.getVariantId(DoubtnutApp.INSTANCE, it)
                if (featureVariant != -1) {
                    userInfo.putAttribute("experiment-${it}", featureVariant)
                }
            }
        }
    }

    fun addUserProperty(configMap: Map<String, Any>) {
        ThreadUtils.runOnAnalyticsThread {
            configMap.forEach {
                when (it.value) {
                    is Boolean -> userInfo.putAttribute(it.key, it.value as Boolean)
                    is String -> userInfo.putAttribute(it.key, it.value as String)
                    is Int -> userInfo.putAttribute(it.key, it.value as Int)
                    is Long -> userInfo.putAttribute(it.key, it.value as Long)
                    is Float -> userInfo.putAttribute(it.key, it.value as Float)
                    is Double -> userInfo.putAttribute(it.key, it.value as Double)
                }
            }
            addDefaultAttributes()
            addAppExperimentAttributes()
            setUserCustomInfo()
        }
    }

    fun setUserIdentifier(id: String) {
        ThreadUtils.runOnAnalyticsThread {
            ApxorSDK.setUserIdentifier(id)
        }
    }

    fun setUserCustomInfo() {
        ThreadUtils.runOnAnalyticsThread {
            ApxorSDK.setUserCustomInfo(userInfo)
        }
    }

    fun setUserCustomInfo(attributes: Attributes) {
        ThreadUtils.runOnAnalyticsThread {
            ApxorSDK.setUserCustomInfo(attributes)
        }
    }

    fun setSessionCustomInfo(attributes: Attributes) {
        ThreadUtils.runOnAnalyticsThread {
            ApxorSDK.setSessionCustomInfo(attributes)
        }
    }

    fun logAppEvent(action: String, attributes: Attributes? = null) {
        ThreadUtils.runOnAnalyticsThread {
            ApxorSDK.logAppEvent(action, attributes)
        }
    }
}