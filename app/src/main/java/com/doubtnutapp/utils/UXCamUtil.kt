package com.doubtnutapp.utils

import android.content.Context
import com.doubtnutapp.BuildConfig
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.data.camerascreen.datasource.LocalConfigDataSource
import com.doubtnutapp.defaultPrefs
import com.uxcam.UXCam
import com.uxcam.datamodel.UXConfig
import java.util.*


/**
 * Created by Anand Gaurav on 2019-12-19.
 */
object UXCamUtil {

    private const val START_UXCAM = 1
    private const val STOP_UXCAM = 2
    private const val USE_DEFAULT_SAMPLING = 3

    fun init() {
        if (shouldStart(Constants.UXCAM_PERCENT)) {
            val config = UXConfig.Builder(BuildConfig.UXCAM_API_KEY)
                .enableAutomaticScreenNameTagging(true)
                .enableImprovedScreenCapture(true)
                .build()
            UXCam.startWithConfiguration(config)
            val studentId = defaultPrefs().getString(Constants.STUDENT_ID, "")
            if (!studentId.isNullOrBlank()) {
                UXCam.setUserIdentity(studentId)
            }

            val cls = defaultPrefs().getString(Constants.STUDENT_CLASS, "")

            if (!cls.isNullOrBlank()) {
                setUserProperty(
                    Constants.STUDENT_CLASS,
                    cls
                )
            }

            val language = defaultPrefs().getString(Constants.STUDENT_LANGUAGE_CODE, "")
            if (!language.isNullOrBlank()) {
                setUserProperty(
                    Constants.LANG_CODE,
                    language
                )
            }

            val campaignName =
                defaultPrefs().getString(LocalConfigDataSource.INSTALL_CAMPAIGN_NAME, "")
            if (!campaignName.isNullOrBlank()) {
                setUserProperty(
                    LocalConfigDataSource.INSTALL_CAMPAIGN_NAME,
                    campaignName
                )
            }
        }
    }

    fun shouldStart(samplingRate: Int, context: Context = DoubtnutApp.INSTANCE): Boolean {
        return when (defaultPrefs().getInt(LocalConfigDataSource.START_UXCAM, STOP_UXCAM)) {
            START_UXCAM -> {
                true
            }
            STOP_UXCAM -> {
                false
            }
            USE_DEFAULT_SAMPLING -> {
                require(!(samplingRate < 1 || samplingRate > 100)) { "samplingRate value must be between 1 - 100" }
                var tracking = defaultPrefs(context).getInt(Constants.TRACK_UX_CAM, -1)
                if (tracking == -1) {
                    tracking = Random().nextInt(100 - 1) + 1
                    defaultPrefs(context).edit().putInt(Constants.TRACK_UX_CAM, tracking).apply()
                }
                tracking <= samplingRate
            }
            else -> {
                false
            }
        }
    }

    fun setUserProperty(key: String, value: String) {
        if (shouldStart(Constants.UXCAM_PERCENT)) {
            UXCam.setUserProperty(key, value)
        }
    }

    fun setUserProperty(key: String, value: Boolean) {
        if (shouldStart(Constants.UXCAM_PERCENT)) {
            UXCam.setUserProperty(key, value)
        }
    }

}