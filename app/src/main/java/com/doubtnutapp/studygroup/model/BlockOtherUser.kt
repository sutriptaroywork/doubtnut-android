package com.doubtnutapp.studygroup.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class BlockOtherUser (
        @SerializedName("message") val message: String?,
        @SerializedName("is_blocked") val isBlocked: Boolean?,
        var itemPosition: Int = -1
)