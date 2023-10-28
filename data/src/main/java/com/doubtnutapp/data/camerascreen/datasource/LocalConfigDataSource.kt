package com.doubtnutapp.data.camerascreen.datasource

import android.content.SharedPreferences
import com.doubtnutapp.data.base.di.qualifier.AppVersionCode
import com.doubtnutapp.domain.camerascreen.entity.CameraConfigEntity
import com.google.gson.Gson
import java.util.*
import javax.inject.Inject

class LocalConfigDataSource @Inject constructor(
    private val sharedPreference: SharedPreferences,
    @AppVersionCode private val appVersionCode: Int
) : ConfigDataSources {

    private val PREF_KEY_CAMERA_SCREEN_HEADLINE_TEXT = "camera_screen_headline_text"
    private val PREF_KEY_TRY_OUT_TEXT = "try_out_text"
    private val PREF_KEY_PICK_GALLERY_BUTTON_TEXT = "pick-gallery_button_text"
    private val PREF_KEY_TYPE_YOUR_DOUBT_TEXT = "type_our_doubt_button_text"
    private val PREF_KEY_DONT_HAVE_QUE_TEXT = "dont_have_ques_text"
    private val CLASS_CAMERA_DATA = "class_camera_data"

    companion object {
        const val PREF_KEY_IS_BOUNTY_ENABLED = "is_bounty_enabled"
        const val PREF_KEY_IS_NCERT_RE_ENTRY_ENABLED = "is_ncert_enabled"
        const val VIDEO_CDN_BASE_URL = "cdn_base_url"
        const val PREF_FORCE_UPDATE = "force_update"
        const val PREF_FORCE_UPDATE_VERSION_CODE = "force_update_version_code"
        const val CAMPAIGN_LANDING_DEEPLINK = "campaign_landing_deeplink"
        const val INSTALL_CAMPAIGN_NAME = "install_campaign_name"
        const val START_UXCAM = "start_uxcam"
    }

    private val DEFAULT_VALUE = ""

    override fun updateCameraScreenConfig(configHashMap: Map<String, Any>) {
        val editor = sharedPreference.edit()

        /*
        for ((key, value) in configHashMap) {
            when (key) {
                PREF_KEY_TRY_OUT_TEXT ->  editor.putString(PREF_KEY_TRY_OUT_TEXT, value)
                PREF_KEY_PICK_GALLERY_BUTTON_TEXT -> editor.putString(PREF_KEY_PICK_GALLERY_BUTTON_TEXT, value)
                PREF_KEY_TYPE_YOUR_DOUBT_TEXT ->  editor.putString(PREF_KEY_TYPE_YOUR_DOUBT_TEXT, value)
                PREF_KEY_CAMERA_SCREEN_HEADLINE_TEXT -> editor.putString(PREF_KEY_CAMERA_SCREEN_HEADLINE_TEXT, value)
                PREF_KEY_DONT_HAVE_QUE_TEXT -> editor.putString(PREF_KEY_DONT_HAVE_QUE_TEXT, value)

            }
        }
         */
        editor.putString(CLASS_CAMERA_DATA, Gson().toJson(configHashMap))
        editor.apply()
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun getCameraScreenConfig(): CameraConfigEntity {
        val headLineText =
            sharedPreference.getString(PREF_KEY_CAMERA_SCREEN_HEADLINE_TEXT, DEFAULT_VALUE)
                .orEmpty()
        val tryOut = sharedPreference.getString(PREF_KEY_TRY_OUT_TEXT, DEFAULT_VALUE).orEmpty()
        val typeYouDoubt =
            sharedPreference.getString(PREF_KEY_TYPE_YOUR_DOUBT_TEXT, DEFAULT_VALUE).orEmpty()
        val pickFromGallery =
            sharedPreference.getString(PREF_KEY_PICK_GALLERY_BUTTON_TEXT, DEFAULT_VALUE).orEmpty()
        val dontHaveQues =
            sharedPreference.getString(PREF_KEY_DONT_HAVE_QUE_TEXT, DEFAULT_VALUE).orEmpty()

        return CameraConfigEntity(
            headLineText,
            tryOut,
            typeYouDoubt,
            pickFromGallery,
            dontHaveQues
        )
    }

    override fun updateEnabledFeatures(configHashMap: Map<String, Any>) {
        sharedPreference.edit().apply {
            putBoolean(
                PREF_KEY_IS_BOUNTY_ENABLED,
                configHashMap["is_bounty_enable"] ?: false as? Boolean == true
            )

            putBoolean(
                PREF_KEY_IS_NCERT_RE_ENTRY_ENABLED,
                configHashMap["ncertReEntry"] ?: false as? Boolean == true
            )
        }.apply()
    }

    override fun setBaseCdnUrl(configHashMap: Map<String, Any>) {
        sharedPreference.edit().apply {
            (configHashMap["video_cdn_base_url"] as? String)?.let {
                putString(VIDEO_CDN_BASE_URL, it)
            }
        }.apply()
    }

    override fun setForceUpdate(configHashMap: HashMap<String, Any>) {
        sharedPreference.edit().apply {
            putInt(PREF_FORCE_UPDATE_VERSION_CODE, appVersionCode)
            (configHashMap[PREF_FORCE_UPDATE] as? Boolean)?.let {
                putBoolean(PREF_FORCE_UPDATE, it)
            } ?: putBoolean(PREF_FORCE_UPDATE, false)
        }.apply()
    }

    override fun setCampaignLandingDeeplink(configHashMap: Map<String, Any>) {
        sharedPreference.edit().apply {
            (configHashMap[CAMPAIGN_LANDING_DEEPLINK] as? String)?.let {
                putString(CAMPAIGN_LANDING_DEEPLINK, it)
            }
            (configHashMap[INSTALL_CAMPAIGN_NAME] as? String)?.let {
                putString(INSTALL_CAMPAIGN_NAME, it)
            }
            (configHashMap[START_UXCAM] as? String)?.toIntOrNull()?.let {
                putInt(START_UXCAM, it)
            }
        }.apply()
    }
}
