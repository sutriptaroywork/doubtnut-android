package com.doubtnutapp.studygroup.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class MuteStudyGroup (
        @SerializedName("message") val message: String?
)