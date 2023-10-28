package com.doubtnutapp.domain.camerascreen.entity

import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
data class AudioTooltipEntity(

    @SerializedName("audio_url")
    val audioUrl: String,

    @SerializedName("image_mute")
    val imageMute: String,

    @SerializedName("image_unmute")
    val imageUnmute: String,

    @SerializedName("screen_name")
    val screenName: String,

    @SerializedName("tooltip_text")
    val tooltipText: String,

    @SerializedName("language")
    val language: String,

    @SerializedName("is_audio_visible")
    val isAudioVisible: Boolean
)
