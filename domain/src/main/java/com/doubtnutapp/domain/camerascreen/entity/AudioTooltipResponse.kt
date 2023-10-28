package com.doubtnutapp.domain.camerascreen.entity

import androidx.annotation.Keep
import com.doubtnut.core.entitiy.AudioTooltipEntity
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
data class AudioTooltipResponse(

    @SerializedName("app_session_count")
    val appSessionCount: Int?,

    @SerializedName("files")
    val files: List<AudioTooltipEntity>?
)
