package com.doubtnutapp.moengage

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.core.content.pm.PackageInfoCompat
import com.apxor.androidsdk.core.Attributes
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.*
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.data.common.UserPreferenceImpl
import com.doubtnutapp.utils.ApxorUtils
import com.doubtnutapp.utils.CountingManager
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.Utils
import com.moe.pushlibrary.MoEHelper
import com.moe.pushlibrary.PayloadBuilder
import com.moengage.core.LogLevel
import com.moengage.core.MoEngage
import com.moengage.core.config.LogConfig
import com.moengage.core.config.MiPushConfig
import com.moengage.core.config.NotificationConfig
import com.moengage.core.model.AppStatus
import com.moengage.firebase.MoEFireBaseHelper
import com.moengage.pushbase.MoEPushHelper
import com.moengage.pushbase.push.PushMessageListener
import java.util.*

object MoEngageUtils {

    fun init(application: Application) {
        val moEngage = MoEngage.Builder(
            application,
            BuildConfig.MOENGAGE_API_KEY
        )
            .configureLogs(
                LogConfig(
                    LogLevel.NO_LOG
                )
            )
            .configureMiPush(
                MiPushConfig(
                    appId = BuildConfig.MI_STORE_APP_ID,
                    appKey = BuildConfig.MI_STORE_APP_KEY,
                    isRegistrationEnabled = true
                )
            )
            .configureNotificationMetaData(
                NotificationConfig(
                    smallIcon = R.mipmap.logo,
                    largeIcon = R.mipmap.logo
                )
            )
            .build()
        MoEngage.initialise(moEngage)
        MoEPushHelper.getInstance().messageListener = MoEngageNotificationListener()
    }

    fun initializeAppStatusToMoEngage(appContext: Context, isFirstTimeInstall: Boolean) {
        if (isFirstTimeInstall) {
            //For Fresh Install of App
            MoEHelper.getInstance(appContext).setAppStatus(AppStatus.INSTALL)
        } else {
            // For Existing user who has updated the app
            if (isUserInstallingUpdate(appContext)) {
                MoEHelper.getInstance(appContext).setAppStatus(AppStatus.UPDATE)
            }
        }
    }

    fun isUserInstallingUpdate(appContext: Context): Boolean =
        getVersionCode(appContext) != defaultPrefs(appContext).getLong(
            Constants.VERSION_CODE,
            0L
        ) && CountingManager.getAppOpenCount(appContext) > 0

    fun updateVersionCodeInPref(appContext: Context) =
        defaultPrefs(appContext).edit().putLong(Constants.VERSION_CODE, getVersionCode(appContext))
            .apply()

    @Deprecated("This should be in core utils with application package name....")
    private fun getVersionCode(appContext: Context): Long {
        val pInfo = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
        return PackageInfoCompat.getLongVersionCode(pInfo)
    }

    fun setStudentData(appContext: Context) {
        setUniqueId(appContext, getStudentId() ?: "")
        setUserAttribute(appContext, Constants.VERSION_NAME, BuildConfig.VERSION_NAME)
        setUserAttribute(appContext, Constants.VERSION_CODE, BuildConfig.VERSION_CODE.toString())
        setOtherUserAttributes(appContext)
    }

    fun setUniqueId(appContext: Context, uniqueId: String) {
        MoEHelper.getInstance(appContext).setUniqueId(uniqueId)
        setAppInstallDate(appContext)
        defaultPrefs(appContext).edit().putBoolean(Constants.IS_MOENGAGE_UNIQUE_ID_SET, true)
            .apply()
    }

    fun setUserAttribute(appContext: Context, key: String, value: String) {
        MoEHelper.getInstance(appContext)
            .setUserAttribute(key, value)
    }

    fun setUserAttribute(appContext: Context, key: String, value: Any?) {
        val moEHelper = MoEHelper.getInstance(appContext)
        when (value) {
            is Boolean -> moEHelper.setUserAttribute(key, value)
            is String -> moEHelper.setUserAttribute(key, value)
            is Int -> moEHelper.setUserAttribute(key, value)
            is Long -> moEHelper.setUserAttribute(key, value)
            is Float -> moEHelper.setUserAttribute(key, value)
            is Double -> moEHelper.setUserAttribute(key, value)
        }
    }

    fun setUserAttribute(appContext: Context, configMap: Map<String, Any>) {
        configMap.forEach {
            setUserAttribute(appContext, it.key, it.value)
        }
    }

    private fun setAppInstallDate(appContext: Context) {
        val installDate =
            Date(
                appContext.packageManager.getPackageInfo(
                    appContext.packageName,
                    0
                ).firstInstallTime
            )
        MoEHelper.getInstance(appContext)
            .setUserAttribute(EventConstants.APP_INSTALL_DATE, installDate)
    }

    private fun setOtherUserAttributes(appContext: Context) {
        val pref = defaultPrefs(appContext)
        val name = pref.getString(Constants.STUDENT_USER_NAME, "") ?: ""
        MoEHelper.getInstance(appContext).apply {
            setFullName(name)
            setNumber(defaultPrefs().getString(Constants.PHONE_NUMBER, ""))
            setUserAttribute(mutableMapOf<String, Any?>().also {
                it[Constants.STUDENT_ID] = pref.getString(Constants.STUDENT_ID, "")
                    ?: ""
                it[Constants.STUDENT_USER_NAME] = pref.getString(Constants.STUDENT_USER_NAME, "")
                    ?: ""
                it[Constants.STUDENT_LANGUAGE_CODE] =
                    pref.getString(Constants.STUDENT_LANGUAGE_CODE, "")
                        ?: ""
                it[Constants.DEVICE_NAME] = pref.getString(Constants.DEVICE_NAME, "")
                    ?: ""
                it[Constants.STUDENT_CLASS] = pref.getString(Constants.STUDENT_CLASS, "")
                    ?: ""
                it[Constants.STUDENT_EXAM] =
                    pref.getString(UserPreferenceImpl.USER_SELECTED_EXAMS, "")?.split(",")

                it[Constants.STUDENT_BOARD] =
                    pref.getString(UserPreferenceImpl.USER_SELECTED_BOARD, "")?.split(",")
            })
        }
    }

    fun trackEvent(appContext: Context, event: String, params: Map<String, Any> = mapOf()) {
        val properties = PayloadBuilder()
        params.mapKeys {
            properties.putAttrObject(it.key, it.value)
        }
        MoEHelper.getInstance(appContext)
            .trackEvent(event, properties)
    }

    fun logoutUser(appContext: Context) {
        MoEHelper.getInstance(appContext).logoutUser()
        defaultPrefs(appContext).edit().putBoolean(Constants.IS_MOENGAGE_UNIQUE_ID_SET, false)
            .apply()
    }

    fun sendPushToken(appContext: Context, token: String) {
        MoEFireBaseHelper.getInstance().passPushToken(appContext, token)
    }
}

class MoEngageNotificationListener : PushMessageListener() {

    override fun onHandleRedirection(activity: Activity, payload: Bundle) {
        super.onHandleRedirection(activity, payload)
        Utils.sendClassLangEvents(EventConstants.EVENT_NAME_APP_OPEN_DN)
        Utils.sendClassLanguageSpecificEvent(EventConstants.EVENT_NAME_APP_OPEN_DN)

        ApxorUtils.logAppEvent(EventConstants.EVENT_NAME_APP_OPEN_DN, Attributes().apply {
            putAttribute(EventConstants.SOURCE, Constants.MOENGAGE)
        })

        DoubtnutApp.INSTANCE.getEventTracker()
            .addEventNames(EventConstants.EVENT_NAME_APP_OPEN_DN)
            .addEventParameter(EventConstants.SOURCE, Constants.MOENGAGE)
            .addStudentId(getStudentId())
            .track()

        // Send to snowplow
        DoubtnutApp.INSTANCE.analyticsPublisher.get().publishEvent(
            StructuredEvent(
                category = EventConstants.EVENT_NAME_APP_OPEN_DN_CATEGORY,
                action = EventConstants.EVENT_NAME_APP_OPEN_DN,
                eventParams = hashMapOf<String, Any>().apply {
                    put(EventConstants.SOURCE, Constants.MOENGAGE)
                }
            )
        )
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun handleCustomAction(appContext: Context?, payload: String) {
        Utils.sendClassLangEvents(EventConstants.EVENT_NAME_APP_OPEN_DN)
        Utils.sendClassLanguageSpecificEvent(EventConstants.EVENT_NAME_APP_OPEN_DN)

        ApxorUtils.logAppEvent(EventConstants.EVENT_NAME_APP_OPEN_DN, Attributes().apply {
            putAttribute(EventConstants.SOURCE, Constants.MOENGAGE)
        })

        DoubtnutApp.INSTANCE.getEventTracker()
            .addEventNames(EventConstants.EVENT_NAME_APP_OPEN_DN)
            .addEventParameter(EventConstants.SOURCE, Constants.MOENGAGE)
            .addStudentId(getStudentId())
            .track()

//        DoubtnutApp.INSTANCE.analyticsPublisher.get().publishBranchIoEvent(
//            AnalyticsEvent(EventConstants.EVENT_NAME_APP_OPEN_DN, hashMapOf<String, Any>().apply {
//                put(EventConstants.SOURCE, Constants.MOENGAGE)
//            })
//        )

        // Send to snowplow
        DoubtnutApp.INSTANCE.analyticsPublisher.get().publishEvent(
            StructuredEvent(
                category = EventConstants.EVENT_NAME_APP_OPEN_DN_CATEGORY,
                action = EventConstants.EVENT_NAME_APP_OPEN_DN,
                eventParams = hashMapOf<String, Any>().apply {
                    put(EventConstants.SOURCE, Constants.MOENGAGE)
                }
            )
        )
    }
}