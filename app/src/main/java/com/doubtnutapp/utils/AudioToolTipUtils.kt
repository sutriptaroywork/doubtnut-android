package com.doubtnutapp.utils

import com.doubtnut.core.entitiy.AudioTooltipEntity
import com.doubtnut.core.view.audiotooltipview.AudioTooltipViewData
import com.doubtnutapp.Constants
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.domain.camerascreen.entity.AudioTooltipResponse
import com.google.gson.Gson
import java.util.*

object AudioToolTipUtils {

    @JvmStatic
    fun isAudioToolTipAvailable(screenName: String): List<AudioTooltipEntity>? {
        val audioData = defaultPrefs().getString(Constants.CAMERA_AUDIO_TOOL_TIP_DATA, "")
        val gson = Gson()
        val dataList: AudioTooltipResponse? =
            gson.fromJson(audioData, AudioTooltipResponse::class.java)
        return dataList?.files?.filter { it.screenName == screenName }
    }

    fun isUserBackToCameraPage(): Boolean {
        val audioData = defaultPrefs().getInt(Constants.USER_BACK_TO_CAMERA_PAGE, 0)
        return audioData == 1
    }


    fun checkForCoreLoopSessionCount(screenName: String): Boolean {
        var coreLoopSessionCount =
            defaultPrefs().getInt(screenName, 0)
        val audioData = defaultPrefs().getString(Constants.CAMERA_AUDIO_TOOL_TIP_DATA, "")
        if (audioData?.isEmpty() == true) {
            return false
        }
        val gson = Gson()
        val data: AudioTooltipResponse? = gson.fromJson(audioData, AudioTooltipResponse::class.java)
        return if (coreLoopSessionCount < (data?.appSessionCount ?: 3) && defaultPrefs().getInt(
                screenName.plus(
                    "_session"
                ), 0
            ) == 0
        ) {
            defaultPrefs().edit()
                .putInt(screenName.plus("_session"), 1).apply()
            defaultPrefs().edit()
                .putInt(screenName, ++coreLoopSessionCount).apply()
            true
        } else {
            false
        }
    }

    fun buildAudioTooltipViewData(itemList: List<AudioTooltipEntity>, screenName: String): AudioTooltipViewData {
        val audioTooltipViewDataBuilder = AudioTooltipViewData.Builder()
        val audioTooltipEntity = if (Locale.getDefault().toString().contains("hi", true) ||
            Locale.getDefault().toString().contains("en", true)
        ) {
            itemList.filter { it.language == "hi" }[0]
        } else {
            itemList.filter { it.language == "en" }[0]
        }
        val tooltipText = if (Locale.getDefault().toString().contains("hi", true)) {
            itemList.filter { it.language == "hi" }[0].tooltipText
        } else {
            itemList.filter { it.language == "en" }[0].tooltipText
        }

        audioTooltipViewDataBuilder
            .audioUrl(audioTooltipEntity.audioUrl)
            .muteImageUrl(audioTooltipEntity.imageMute)
            .screenName(audioTooltipEntity.screenName)
            .unMuteImageUrl(audioTooltipEntity.imageUnmute)
            .screenName(screenName)
            .tooltipText(tooltipText)

        return audioTooltipViewDataBuilder.build()
    }
}