package com.doubtnutapp.data.camerascreen.datasource

import android.content.SharedPreferences
import com.doubtnutapp.domain.camerascreen.entity.CropScreenConfigEntity
import javax.inject.Inject

class LocalCropScreenConfigDataSource @Inject constructor(
    private val sharedPreference: SharedPreferences
) : CropScreenConfigDataSource {

    private val PREF_KEY_CROP_SCREEN_HEADLINE_TEXT = "crop_screen_headline_text"
    private val PREF_KEY_IMAGE_URI = "sample_image_uri"
    private val PREF_KEY_FIND_SOLUTION_BUTTON_TEXT = "find_solution_button_text"

    private val DEFAULT_VALUE = ""

    override fun saveCropScreenConfig(configHashMap: Map<String, String>) {
        val editor = sharedPreference.edit()

        for ((key, value) in configHashMap) {
            when (key) {
                PREF_KEY_CROP_SCREEN_HEADLINE_TEXT -> editor.putString(PREF_KEY_CROP_SCREEN_HEADLINE_TEXT, value)
                PREF_KEY_IMAGE_URI -> editor.putString(PREF_KEY_IMAGE_URI, value)
                PREF_KEY_FIND_SOLUTION_BUTTON_TEXT -> editor.putString(PREF_KEY_FIND_SOLUTION_BUTTON_TEXT, value)
            }
        }

        editor.apply()
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun getCropScreenConfig(): CropScreenConfigEntity {

        val headLineTitleText = sharedPreference.getString(PREF_KEY_CROP_SCREEN_HEADLINE_TEXT, DEFAULT_VALUE).orEmpty()
        val sampleImageUrl = sharedPreference.getString(PREF_KEY_IMAGE_URI, DEFAULT_VALUE).orEmpty()
        val findSolutionButtonText = sharedPreference.getString(PREF_KEY_FIND_SOLUTION_BUTTON_TEXT, DEFAULT_VALUE).orEmpty()

        return CropScreenConfigEntity(
            headLineTitleText,
            sampleImageUrl,
            findSolutionButtonText
        )
    }
}
